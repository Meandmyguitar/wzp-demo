package com.lanmaoly.cloud.support.amqp;

import com.rabbitmq.client.AMQP;
import reactor.rabbitmq.OutboundMessage;

/**
 * 持久化的Amqp消息
 */
public class PersistentAmqpMessage extends OutboundMessage {

    private long id;

    PersistentAmqpMessage(long id, String routingKey, byte[] body, String exchange) {
        this(id, routingKey, null, body, exchange);
    }

    PersistentAmqpMessage(long id, String routingKey, AMQP.BasicProperties properties, byte[] body, String exchange) {
        super(exchange, routingKey, properties, body);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
