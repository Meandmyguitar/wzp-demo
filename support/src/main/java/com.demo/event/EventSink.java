package com.lanmaoly.cloud.support.event;

import java.io.Serializable;
import java.util.function.Consumer;

public interface EventSink<T extends Serializable> {

    void subscribe(Consumer<T> listener);
}
