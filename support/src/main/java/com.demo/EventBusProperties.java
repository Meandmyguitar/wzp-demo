package com.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "demo.event")
public class EventBusProperties {

    private EventBusType type = EventBusType.AMQP;

    private AmqpEventBus amqp = new AmqpEventBus();

    public EventBusType getType() {
        return type;
    }

    public void setType(EventBusType type) {
        this.type = type;
    }

    public AmqpEventBus getAmqp() {
        return amqp;
    }

    public void setAmqp(AmqpEventBus amqp) {
        this.amqp = amqp;
    }

    public enum EventBusType {

        SIMPLE,

        AMQP
    }

    public static class AmqpEventBus {
        /**
         * rabbitmqURL
         */
        private URI rabbitUrl;

        /**
         * 回压缓冲区大小，到超过此缓冲区时，最新的消息会被丢弃
         */
        private int backpressureBufferSize = 10000;

        /**
         * 消费消息的最大重试次数
         */
        private int consumeMaxRetryNum = 3;

        /**
         * 两次定时补偿的时间间隔
         */
        private Duration retryInterval = Duration.ofSeconds(300);

        /**
         * 补偿的最近时间间隔，晚于此时间的不做补偿
         */
        private Duration retryRecently = Duration.ofMinutes(10);

        /**
         * 补偿的最远时间点，早于此时间的不做补偿
         */
        private Duration retryFarthest = Duration.ofDays(1);

        /**
         * 可以并行处理的事件数量
         */
        private int parallelism = 4;

        public URI getRabbitUrl() {
            return rabbitUrl;
        }

        public void setRabbitUrl(URI rabbitUrl) {
            this.rabbitUrl = rabbitUrl;
        }

        public int getBackpressureBufferSize() {
            return backpressureBufferSize;
        }

        public void setBackpressureBufferSize(int backpressureBufferSize) {
            this.backpressureBufferSize = backpressureBufferSize;
        }

        public int getConsumeMaxRetryNum() {
            return consumeMaxRetryNum;
        }

        public void setConsumeMaxRetryNum(int consumeMaxRetryNum) {
            this.consumeMaxRetryNum = consumeMaxRetryNum;
        }

        public Duration getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Duration retryInterval) {
            this.retryInterval = retryInterval;
        }

        public Duration getRetryRecently() {
            return retryRecently;
        }

        public void setRetryRecently(Duration retryRecently) {
            this.retryRecently = retryRecently;
        }

        public Duration getRetryFarthest() {
            return retryFarthest;
        }

        public void setRetryFarthest(Duration retryFarthest) {
            this.retryFarthest = retryFarthest;
        }

        public int getParallelism() {
            return parallelism;
        }

        public void setParallelism(int parallelism) {
            this.parallelism = parallelism;
        }
    }
}
