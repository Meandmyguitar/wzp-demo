package com.wzp.cloud.support.event;

import com.wzp.cloud.support.msgbus.MessageBus;
import com.wzp.cloud.support.msgbus.SubscribeOption;

import java.util.function.Consumer;

public class MessageBusEventSpi implements EventBusSpi {

    private final MessageBus messageBus;

    public MessageBusEventSpi(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    @Override
    public void fire(String eventName, byte[] data) {
        messageBus.publish(data, eventName);
    }

    @Override
    public void subscribe(String group, String eventName, Consumer<byte[]> listener) {
        messageBus.subscribe((message, t) -> listener.accept(message), SubscribeOption.of(group, eventName));
    }
}
