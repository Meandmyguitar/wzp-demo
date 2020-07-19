package com.wzp.util.pubsub;

public class NoOpPubSub implements PubSub {

    @Override
    public void publishAsync(String topic, Object message) {

    }

    @Override
    public void publishSync(String topic, Object message) {

    }

    @Override
    public void subscribe(String topic, Listener listener) {

    }

    @Override
    public void subscribeSync(String topic, Listener listener) {

    }

    @Override
    public <T> Event<T> defineEvent(String topic, Class<T> eventType) {
        return new Event<>(this, topic);
    }
}
