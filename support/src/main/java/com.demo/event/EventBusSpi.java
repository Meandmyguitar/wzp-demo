package com.demo.event;

import java.util.function.Consumer;

public interface EventBusSpi {

    void fire(String eventName, byte[] data);

    void subscribe(String group, String eventName, Consumer<byte[]> listener);
}
