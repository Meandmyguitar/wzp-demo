package com.wzp.util.pubsub;

/**
 * 事件的订阅和发布，支持同步和异步两种模式
 *
 * <p>
 *     同步事件，事件发布代码和事件处理代码在同一个线程，并且是同步处理。
 * </p>
 * <p>
 *     异步事件。事件发布代码和事件处理代码不在同一个线程。
 * </p>
 */
public interface PubSub {

    /**
     * 异步发布事件
     */
    void publishAsync(String topic, Object message);

    /**
     * 发布同步事件
     */
    void publishSync(String topic, Object message);

    /**
     * 订阅异步事件
     */
    void subscribe(String topic, Listener listener);

    /**
     * 订阅同步事件
     */
    void subscribeSync(String topic, Listener listener);

    /**
     * 定义一个事件，事件可以被订阅或者发布消息
     * @param topic 事件的topic
     * @param eventType 事件类
     * @return 事件实例
     */
    <T> Event<T> defineEvent(String topic, Class<T> eventType);
}
