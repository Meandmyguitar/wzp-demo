package com.wzp.util.pubsub;

/**
 * 事件监听接口
 */
@FunctionalInterface
public interface Listener<T> {

    void onMessage(String topic, T message) throws Exception;
}
