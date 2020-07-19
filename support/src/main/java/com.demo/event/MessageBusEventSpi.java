package com.demo.event;

import com.demo.msgbus.MessageBus;
import com.demo.msgbus.SubscribeOption;

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
