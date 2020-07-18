package com.lanmaoly.cloud.support.msgbus;

/**
 * 事件消息监听接口
 */
@FunctionalInterface
public interface MessageListener {

    void onMessage(byte[] message, String topic) throws Exception;
}
