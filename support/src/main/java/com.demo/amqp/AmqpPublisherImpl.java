package com.lanmaoly.cloud.support.amqp;

import com.lanmaoly.cloud.support.lock.DistributedLock;
import com.lanmaoly.cloud.support.lock.DistributedLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class AmqpPublisherImpl implements AmqpPublisher, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(AmqpPublisherImpl.class);

    private final AmqpCorePublisher publisher;

    private final Executor workExecutor;

    private final ScheduledFuture<?> future;

    private final DistributedLock lock;

    public AmqpPublisherImpl(
            AmqpTemplate amqpTemplate,
            DistributedLockManager lockManager,
            DataSource dataSource,
            ScheduledExecutorService scheduledExecutor,
            Executor workExecutor,
            Duration retryInterval,
            Duration retryBefore) {
        this.publisher = new AmqpCorePublisher(amqpTemplate, dataSource);
        this.workExecutor = workExecutor;
        try {
            lock = lockManager.newLock("/sys/amqp-publisher");
        } catch (InterruptedException e) {
            throw new IllegalStateException("创建分布式锁失败", e);
        }

        // 重试线程
        future = scheduledExecutor.scheduleWithFixedDelay(
                () -> retry(retryBefore),
                retryInterval.toMillis(),
                retryInterval.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        future.cancel(false);
    }

    @Override
    public void send(String exchange, String routingKey, Envelope envelope) {
        Map<String, String> map = inject();
        publisher.send(exchange, routingKey, envelope, workExecutor, map);
    }

    @Override
    public void sendSync(String exchange, String routingKey, Envelope envelope) {
        Map<String, String> map = inject();
        publisher.send(exchange, routingKey, envelope, Runnable::run, map);
    }

    private Map<String, String> inject() {
        return Collections.emptyMap();
    }

    private void retry(Duration retryBefore) {
        try {
            logger.info("执行补发检查");
            lock.lock();
            publisher.retry(retryBefore);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
}
