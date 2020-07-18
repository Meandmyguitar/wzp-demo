package com.lanmaoly.cloud.support.msgbus;

import com.lanmaoly.cloud.support.PersistentAmqpMessageResult;
import com.lanmaoly.cloud.support.amqp.PersistentAmqpMessage;
import com.lanmaoly.cloud.support.amqp.ReactivePersistentAmqpSender;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.*;
import reactor.rabbitmq.*;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@SuppressWarnings("WeakerAccess")
public class ReactiveAmqpMessageBus implements MessageBus, AutoCloseable {

    public static final String H_TOPIC = "EVTBUS-TOPIC";

    public static final String H_GROUP = "EVTBUS-GROUP";

    public static final String EXCHANGE = "eventbus";

    public static final String DLX = "eventbus.dlx";

    private final Logger logger = LoggerFactory.getLogger(ReactiveAmqpMessageBus.class);

    private ReactiveAmqpMessageBusOption option;

    private Duration timeout = Duration.ofSeconds(30);

    private Sender sender;

    private Receiver receiver;

    private ReactivePersistentAmqpSender persistentAmqpSender;

    private FluxSink<PersistentAmqpMessage> sink;

    private Map<String, GroupDispatcher> groupDispatchers = new ConcurrentHashMap<>();

    private EmitterProcessor<AcknowledgableDelivery> queueProcessor = EmitterProcessor.create();

    private EmitterProcessor<AcknowledgableDelivery> deadLetterProcessor = EmitterProcessor.create();

    private Flux<AcknowledgableDelivery> mergedFlux;

    public ReactiveAmqpMessageBus(ConnectionFactory connectionFactory, DataSource dataSource, ReactiveAmqpMessageBusOption option) {
        this(RabbitFlux.createSender(new SenderOptions()
                        .connectionFactory(connectionFactory)
                        .resourceManagementScheduler(option.getScheduler())),
                RabbitFlux.createReceiver(new ReceiverOptions()
                        .connectionFactory(connectionFactory)
                        .connectionSubscriptionScheduler(option.getScheduler())),
                dataSource,
                option);
    }

    public ReactiveAmqpMessageBus(Sender sender, Receiver receiver, DataSource dataSource, ReactiveAmqpMessageBusOption option) {
        this.option = option;

        this.sender = sender;
        this.receiver = receiver;

        // 定义必须的exchange和queue
        declareAmqp(option.getGroup());

        // 配置发送流的回压缓冲区以及持久化
        EmitterProcessor<PersistentAmqpMessage> processor = EmitterProcessor.create();
        sink = processor.sink();
        persistentAmqpSender = new ReactivePersistentAmqpSender(dataSource);
        Flux<PersistentAmqpMessage> flux = processor.onBackpressureBuffer(
                option.getBackpressureBufferSize(),
                m -> logger.warn("缓冲区已满消息被丢弃: id={}", m.getId()),
                BufferOverflowStrategy.DROP_LATEST);
        persistentAmqpSender.send(sender, flux).subscribe();

        // 配置接收流
        mergedFlux = Flux.merge(queueProcessor, deadLetterProcessor);
        mergedFlux.filter(this::dropInvalidMessage)
                .parallel(option.getParallelism()).runOn(option.getScheduler())
                .flatMap(this::dispatch)
                .subscribe();
    }

    private Mono<? extends AcknowledgableDelivery> dispatch(AcknowledgableDelivery message) {
        MessageHeader header = getMessageHeader(message);
        GroupDispatcher groupDispatcher = groupDispatchers.get(header.getGroup());
        if (groupDispatcher != null) {
            TopicDispatcher topicDispatcher = groupDispatcher.topicDispatchers.get(header.getTopic());
            if (topicDispatcher != null) {
                return topicDispatcher.dispatch(message).retryBackoff(option.getConsumeMaxRetryNum(), Duration.ofSeconds(1), Duration.ofSeconds(1), option.getScheduler())
                        .onErrorResume(ex -> {
                            logger.info("重试失败", ex);
                            message.nack(header.isDeadLetter());    // 死信不能丢弃必须requeue
                            return Mono.just(message);
                        });
            }
        }

        // 对于未订阅的消息直接ack
        return Mono.fromCallable(() -> {
            message.ack();
            return message;
        });
    }

    @Override
    public String getGroup() {
        return option.getGroup();
    }

    @Override
    public void publish(byte[] message, String topic) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("未开启事务不能发送");
        }

        String realGroup = this.option.getGroup();

        Map<String, Object> header = new HashMap<>();
        header.put(H_TOPIC, topic);
        header.put(H_GROUP, realGroup);
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .deliveryMode(2).headers(header).build();
        PersistentAmqpMessage msg = persistentAmqpSender.persistent(EXCHANGE, realGroup + "." + topic, message, basicProperties);

        // 事务提交后发送消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sink.next(msg);
            }
        });
    }

    @Override
    public void subscribe(MessageListener listener, SubscribeOption option) {
        GroupDispatcher holder = groupDispatchers.computeIfAbsent(
                option.getGroup(),
                k -> new GroupDispatcher(option.getGroup(), mergedFlux));

        holder.add(new ListenerHolder(listener, option));
    }

    @Override
    public void close() {
        sender.close();
        receiver.close();
    }

    public Flux<PersistentAmqpMessageResult> retry(LocalDateTime start, LocalDateTime end, int retryMaxFailCount) {
        return persistentAmqpSender.send(sender, persistentAmqpSender.query(start, end, retryMaxFailCount));
    }

    /**
     * 开始消费队列消息
     */
    public void startConsume() {
        consumeQueue(option.getNameOfQueue(), queueProcessor);
        logger.info("start consume queue [{}]", option.getNameOfQueue());
    }

    /**
     * 开始消费死信队列
     */
    public void startConsumeDeadLetter() {
        consumeQueue(option.getNameOfDeadLetterQueue(), deadLetterProcessor);
        logger.info("start consume dead letter queue [{}]", option.getNameOfDeadLetterQueue());
    }

    /**
     * 删除全局以及私有的exchange和queue
     * <p>警告！此为破坏性方法，需谨慎调用</p>
     */
    public void clear() {
        sender.delete(QueueSpecification.queue(option.getNameOfQueue())).block();
        sender.delete(QueueSpecification.queue(option.getNameOfDeadLetterQueue())).block();
        sender.delete(ExchangeSpecification.exchange(ReactiveAmqpMessageBus.DLX)).block();
        sender.delete(ExchangeSpecification.exchange(ReactiveAmqpMessageBus.EXCHANGE)).block();
    }

    private void consumeQueue(String queue, CoreSubscriber<AcknowledgableDelivery> actual) {

        // 重试策略
        BiConsumer<Receiver.AcknowledgmentContext, Exception> exceptionHandler =
                new ExceptionHandlers.RetryAcknowledgmentExceptionHandler(
                        Duration.ofSeconds(20), Duration.ofMillis(500),
                        ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE
                );
        // 消费队列
        receiver.consumeManualAck(queue, new ConsumeOptions().exceptionHandler(exceptionHandler))
                .filter(this::dropInvalidMessage)
                .subscribe(actual);
    }

    private void declareAmqp(String group) {
        // 定义全局DLX
        sender.declareExchange(ExchangeSpecification.exchange(DLX).type("direct").durable(true).autoDelete(false)).block(timeout);
        sender.declareQueue(QueueSpecification.queue(option.getNameOfDeadLetterQueue()).durable(true)).block(timeout);
        sender.bind(BindingSpecification.binding(DLX, group, option.getNameOfDeadLetterQueue())).block(timeout);

        // 定义全局exchange
        sender.declareExchange(ExchangeSpecification.exchange(EXCHANGE).type("topic").durable(true).autoDelete(false)).block(timeout);

        // 定义当前group的queue,设置dlx
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", DLX);
        arguments.put("x-dead-letter-routing-key", group);
        sender.declareQueue(QueueSpecification.queue(option.getNameOfQueue()).durable(true).arguments(arguments)).block(timeout);
    }

    /**
     * 过滤非法的消息
     */
    private boolean dropInvalidMessage(AcknowledgableDelivery message) {
        try {
            getMessageHeader(message);
        } catch (MessageBusException e) {
            message.ack();
            logger.warn("忽略非法的消息: {} tag={}", e.getMessage(), message.getEnvelope().getDeliveryTag());
            return false;
        }
        return true;
    }

    private MessageHeader getMessageHeader(AcknowledgableDelivery m) {
        Map<String, Object> headers = m.getProperties().getHeaders();
        if (headers == null) {
            throw new MessageBusException("消息header未包含" + H_TOPIC + "和" + H_GROUP);
        }
        String topic = headers.getOrDefault(H_TOPIC, "").toString();
        String group = headers.getOrDefault(H_GROUP, "").toString();
        Object death = headers.get("x-death");
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(group)) {
            throw new MessageBusException("消息header未包含" + H_TOPIC + "和" + H_GROUP);
        }
        return new MessageHeader(group, topic, death);
    }

    class GroupDispatcher {

        private final Map<String, TopicDispatcher> topicDispatchers = new ConcurrentHashMap<>();

        private Flux<AcknowledgableDelivery> flux;

        public GroupDispatcher(String group, Flux<AcknowledgableDelivery> source) {
            sender.bind(BindingSpecification.binding(EXCHANGE, group + ".*", option.getNameOfQueue())).block();

            EmitterProcessor<AcknowledgableDelivery> processor = EmitterProcessor.create();
            flux = processor.filter(message -> {
                MessageHeader header = getMessageHeader(message);
                boolean b = group.equals(header.getGroup());
                if (b) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("group dispatch: group={}, topic={}, tag={}", group, header.getTopic(), message.getEnvelope().getDeliveryTag());
                    }
                } else {
                    message.ack();
                }
                return b;
            });
//            source.subscribe(processor);
        }

        public void add(ListenerHolder listener) {
            TopicDispatcher topicDispatcher = topicDispatchers.compute(listener.getOption().getTopic(), (k, v) -> {
                if (v == null) {
                    return new TopicDispatcher(flux.filter(message -> {
                        MessageHeader header = getMessageHeader(message);
                        return k.equals(header.getTopic());
                    }), k, option.getParallelism());
                } else {
                    return v;
                }
            });
            topicDispatcher.add(listener);
        }
    }

    class TopicDispatcher {

        private Flux<AcknowledgableDelivery> source;

        private String topic;

        private volatile List<ListenerHolder> holders = Collections.emptyList();

        public TopicDispatcher(Flux<AcknowledgableDelivery> source, String topic, int parallelism) {
            this.topic = topic;
            this.source = source.filter(message -> {
                MessageHeader header = getMessageHeader(message);
                boolean b = topic.equals(header.getTopic());
                if (b) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("TopicDispatcher: topic={}, tag={}", topic, message.getEnvelope().getDeliveryTag());
                    }
                } else {
                    message.ack();
                }
                return b;
            });

//            subscribe(parallelism);
        }

        public void add(ListenerHolder holders) {
            add(Collections.singletonList(holders));
        }

        public synchronized void add(Collection<ListenerHolder> holders) {
            ArrayList<ListenerHolder> temp = new ArrayList<>(this.holders);
            temp.addAll(holders);
            this.holders = Collections.unmodifiableList(temp);
        }

        private void subscribe(int parallelism) {
            source.parallel(parallelism)
                    .runOn(option.getScheduler())
                    .flatMap(message -> dispatch(message)
                            .retryBackoff(option.getConsumeMaxRetryNum(), Duration.ofSeconds(1), Duration.ofSeconds(1), option.getScheduler())
                            .onErrorResume(ex -> {
                                logger.info("重试失败", ex);
                                message.nack(false);
                                return Mono.just(message);
                            })).subscribe();
        }

        private Mono<AcknowledgableDelivery> dispatch(AcknowledgableDelivery message) {
            return Mono.just(message).map(msg -> {
                MessageHeader header = getMessageHeader(msg);
                try {
                    for (ListenerHolder holder : holders) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("dispatch listener. topic={} tag={}, deadletter={}", topic, msg.getEnvelope().getDeliveryTag(), header.isDeadLetter());
                        }
                        holder.getListener().onMessage(msg.getBody(), topic);
                    }
                    msg.ack();
                    return msg;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    private static class ListenerHolder {

        private MessageListener listener;

        private SubscribeOption option;

        public ListenerHolder(MessageListener listener, SubscribeOption option) {
            this.listener = listener;
            this.option = option;
        }

        public MessageListener getListener() {
            return listener;
        }

        public SubscribeOption getOption() {
            return option;
        }
    }

    static class MessageHeader {

        private String group;

        private String topic;

        private Object death;

        public MessageHeader(String group, String topic, Object death) {
            this.group = group;
            this.topic = topic;
            this.death = death;
        }

        public String getGroup() {
            return group;
        }

        public String getTopic() {
            return topic;
        }

        public Object getDeath() {
            return death;
        }

        public boolean isDeadLetter() {
            return death != null;
        }
    }
}
