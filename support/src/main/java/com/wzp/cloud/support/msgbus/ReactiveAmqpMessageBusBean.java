package com.wzp.cloud.support.msgbus;

import com.wzp.cloud.support.lock.DistributedLock;
import com.wzp.cloud.support.lock.DistributedLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ReactiveAmqpMessageBusBean implements MessageBus, ApplicationListener, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(ReactiveAmqpMessageBusBean.class);

    private ReactiveAmqpMessageBus messageBus;

    private ScheduledExecutorService executor;

    private final DistributedLock lock;

    private ScheduledFuture<?> future;

    /**
     * 两次定时补偿的时间间隔
     */
    private Duration retryInterval;

    /**
     * 补偿的最近时间间隔，晚于此时间的不做补偿
     */
    private Duration retryRecently;

    /**
     * 补偿的最远时间点，早于此时间的不做补偿
     */
    private Duration retryFarthest;

    private int retryMaxFailCount;

    public ReactiveAmqpMessageBusBean(
            ReactiveAmqpMessageBus messageBus,
            ScheduledExecutorService executor,
            DistributedLockManager lockManager,
            Duration retryInterval,
            Duration retryRecently,
            Duration retryFarthest,
            int retryMaxFailCount) {
        this.messageBus = messageBus;
        this.executor = executor;
        this.retryInterval = retryInterval;
        this.retryRecently = retryRecently;
        this.retryFarthest = retryFarthest;
        this.retryMaxFailCount = retryMaxFailCount;

        try {
            lock = lockManager.newLock("/sys/amqp-message");
        } catch (InterruptedException e) {
            throw new IllegalStateException("创建分布式锁失败", e);
        }
    }

    @Override
    public String getGroup() {
        return messageBus.getGroup();
    }

    @Override
    public void publish(byte[] message, String topic) {
        messageBus.publish(message, topic);
    }

    @Override
    public void subscribe(MessageListener listener, SubscribeOption option) {
        messageBus.subscribe(listener, option);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshed();
        }
    }

    @Override
    public void close() {

        future.cancel(true);

        messageBus.close();
    }

    private void onContextRefreshed() {

        messageBus.startConsume();

        future = executor.scheduleWithFixedDelay(
                this::schedule,
                10, retryInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void schedule() {
        try {
            lock.lock();

            logger.info("定时补偿开始");

            LocalDateTime end = LocalDateTime.now().minus(retryRecently);
            LocalDateTime start = end.minus(retryFarthest);

            Integer total = messageBus.retry(start, end, retryMaxFailCount).reduce(0, (a, b) -> a + 1).block();
            logger.info("定时补偿结束: 补偿总数={}", total);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
}
