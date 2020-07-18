package com.lanmaoly.cloud.support;

import com.lanmaoly.cloud.support.amqp.PersistentAmqpMessage;
import reactor.rabbitmq.OutboundMessageResult;

public class PersistentAmqpMessageResult {

    private OutboundMessageResult result;

    public PersistentAmqpMessageResult(OutboundMessageResult result) {
        this.result = result;
    }

    public PersistentAmqpMessage getOutboundMessage() {
        return (PersistentAmqpMessage) result.getOutboundMessage();
    }

    public boolean isAck() {
        return result.isAck();
    }
}
