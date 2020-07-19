package com.wzp.cloud.support;

import com.wzp.cloud.support.amqp.PersistentAmqpMessage;
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
