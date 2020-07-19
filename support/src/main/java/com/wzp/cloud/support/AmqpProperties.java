package com.wzp.cloud.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "demo.amqp")
public class AmqpProperties {

    /**
     * 异步发送时，本地缓冲队列的最大长度，当队列满时会直接丢弃等待补发
     */
    @Value("10000")
    private int bufferQueueMaxSize;

    /**
     * 消息补发定时的时间间隔
     */
    @Value("10m")
    private Duration retryInterval;

    /**
     * 消息等待多长时间后才被补发
     */
    @Value("10m")
    private Duration retryBefore;

    public int getBufferQueueMaxSize() {
        return bufferQueueMaxSize;
    }

    public void setBufferQueueMaxSize(int bufferQueueMaxSize) {
        this.bufferQueueMaxSize = bufferQueueMaxSize;
    }

    public Duration getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Duration retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Duration getRetryBefore() {
        return retryBefore;
    }

    public void setRetryBefore(Duration retryBefore) {
        this.retryBefore = retryBefore;
    }
}
