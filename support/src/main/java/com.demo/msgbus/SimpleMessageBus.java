package com.demo.msgbus;

import com.lanmaoly.util.lang.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 简单的MessageBus，可用于单元测试
 */
public class SimpleMessageBus implements MessageBus {

    private final Logger logger = LoggerFactory.getLogger(SimpleMessageBus.class);

    private String group;

    private FluxSink<SimpleMessage> sink;

    private volatile List<MessageListenerHolder> listeners = Collections.emptyList();

    public SimpleMessageBus(String group) {
        this.group = group;

        EmitterProcessor<SimpleMessage> processor = EmitterProcessor.create();
        sink = processor.sink();

        processor.onBackpressureBuffer(10000,
                m -> logger.warn("缓冲区已满消息被丢弃: topic={}", m.getTopic()),
                BufferOverflowStrategy.DROP_LATEST).publishOn(Schedulers.elastic()).flatMap(message -> Mono.fromRunnable(() -> {
            try {
                for (MessageListenerHolder holder : listeners) {
                    if (holder.topic.equals(message.getTopic())) {
                        holder.listener.onMessage(message.getData(), message.getTopic());
                    }
                }
            } catch (Exception e) {
                throw ExceptionUtils.throwUnchecked(e);
            }
        }).onErrorResume(ex -> {
            logger.info("重试失败", ex);
            return Mono.just(message);
        })).subscribe();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void publish(byte[] message, String topic) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("未开启事务不能发送");
        }

        // 事务提交后发送消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sink.next(new SimpleMessage(topic, message));
            }
        });
    }

    @Override
    public void subscribe(MessageListener listener, SubscribeOption option) {
        if (option.getGroup().equals(group)) {
            ArrayList<MessageListenerHolder> temp = new ArrayList<>(listeners);
            temp.add(new MessageListenerHolder(listener, option.getTopic()));
            this.listeners = temp;
        }
    }

    static class SimpleMessage {

        private String topic;

        private byte[] data;

        SimpleMessage(String topic, byte[] data) {
            this.topic = topic;
            this.data = data;
        }

        String getTopic() {
            return topic;
        }

        public byte[] getData() {
            return data;
        }
    }

    static class MessageListenerHolder {

        private MessageListener listener;

        private String topic;

        public MessageListenerHolder(MessageListener listener, String topic) {
            this.listener = listener;
            this.topic = topic;
        }
    }
}
