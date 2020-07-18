package com.lanmaoly.cloud.support.msgbus;

public interface MessageBus {

    String getGroup();

    /**
     * 发布一个消息
     */
    void publish(byte[] message, String topic);

    /**
     * 订阅消息
     */
    void subscribe(MessageListener listener, SubscribeOption option);
}
