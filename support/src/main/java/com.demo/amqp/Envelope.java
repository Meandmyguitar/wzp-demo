package com.demo.amqp;

import java.io.Serializable;

/**
 * 消息的包装
 */
@SuppressWarnings("WeakerAccess")
public final class Envelope implements Serializable {

    private final Serializable payload;

    public Envelope(Serializable payload) {
        this.payload = payload;
    }

    public Serializable getPayload() {
        return payload;
    }
}
