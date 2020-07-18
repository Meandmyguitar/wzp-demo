package com.lanmaoly.cloud.support.event;

import com.lanmaoly.cloud.support.msgbus.MessageBus;
import com.lanmaoly.cloud.support.msgbus.SubscribeOption;

import java.util.function.Consumer;

public class SimpleEventBusSpi implements EventBusSpi {

    private MessageBus messageBus;

    public SimpleEventBusSpi(MessageBus messageBus) {
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
