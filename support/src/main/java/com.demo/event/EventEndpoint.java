package com.demo.event;

import java.io.Serializable;

public interface EventEndpoint<T extends Serializable> extends EventPublisher<T>, EventSink<T> {

    String getName();

    String getGroup();

    EventSink<T> createSink();
}
