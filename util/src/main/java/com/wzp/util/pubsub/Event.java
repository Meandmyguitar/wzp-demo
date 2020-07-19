package com.wzp.util.pubsub;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class Event<T> {

    private final Logger logger = LoggerFactory.getLogger(Event.class);

    private PubSub pubsub;

    private String topic;

    @SuppressWarnings("WeakerAccess")
    public Event(PubSub pubsub, String topic) {
        this.pubsub = pubsub;
        this.topic = topic;
    }

    public void subscribe(Listener<T> listener) {
        pubsub.subscribe(topic, listener);
    }

    public void subscribeSync(Listener<T> listener) {
        pubsub.subscribeSync(topic, listener);
    }

    public void publish(T message) {
        pubsub.publishSync(topic, message);
        pubsub.publishAsync(topic, message);
    }

    /**
     * 在事务中发布事件
     *
     * @throws IllegalStateException 当前不在事务处理范围
     */
    public void publishInTransaction(T message) {
        if (logger.isDebugEnabled()) {
            logger.debug("publishInTransaction, message:" + message);
        }

        // 事务提交后发送通知
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            pubsub.publishSync(topic, message);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pubsub.publishAsync(topic, message);
                }
            });
        } else {
            throw new IllegalStateException("当前没有开启事务");
        }
    }
}
