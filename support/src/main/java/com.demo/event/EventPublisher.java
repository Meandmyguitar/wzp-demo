package com.demo.event;

import java.io.Serializable;

public interface EventPublisher<T extends Serializable> {

    void fire(T event);
}
