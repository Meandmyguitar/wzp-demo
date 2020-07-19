package com.wzp.cloud.support.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzp.cloud.support.JacksonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class EventBus {

    private final ObjectMapper mapper = JacksonBuilder.build().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private Map<String, EventGroup> groups = new ConcurrentHashMap<>();

    private EventBusSpi bus;

    private String group;

    @SuppressWarnings("WeakerAccess")
    public EventBus(EventBusSpi bus, String group) {
        this.bus = bus;
        this.group = group;
    }

    @SuppressWarnings("unused")
    public <T extends Serializable> EventSink<T> createSink(String group, String name, Class<T> eventType) {
        EventEndpointImpl<T> sink = new EventEndpointImpl<>(name, group, eventType);
        register(group, sink);
        return sink.createSink();
    }

    @SuppressWarnings("WeakerAccess")
    public <T extends Serializable> EventEndpoint<T> createEndpoint(String name, Class<T> eventType) {
        EventEndpointImpl<T> publisher = new EventEndpointImpl<>(name, group, eventType);
        register(group, publisher);
        return publisher;
    }

    <T extends Serializable> void fire(String name, T e) {
        byte[] data = toData(e);
        bus.fire(name, data);
    }

    private <T extends Serializable> void register(String group, EventEndpointImpl<T> sink) {
        EventGroup eventGroup = groups.computeIfAbsent(group, EventGroup::new);
        eventGroup.register(sink);
        bus.subscribe(sink.getGroup(), sink.getName(), (data) -> {
            sink.dispatch(fromData(data, sink.getEventType()));
        });
    }

    private <T> T fromData(byte[] message, Class<T> type) {
        try {
            return mapper.readValue(message, type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> byte[] toData(T event) {
        try {
            return mapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    class EventGroup {

        private String group;

        private Map<String, EventEndpointImpl> events;

        EventGroup(String group) {
            this.group = group;
            events = new ConcurrentHashMap<>();
        }

        <T extends Serializable> void register(EventEndpointImpl<T> eventDeclare) {
            if (events.putIfAbsent(eventDeclare.getName(), eventDeclare) != null) {
                throw new IllegalStateException("Event重复定义: group=" + group + ", name=" + eventDeclare.getName());
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class EventBaseSink<T extends Serializable> implements EventSink<T> {

        private String name;

        private String group;

        private Class<T> eventType;

        private volatile List<Consumer<T>> listeners = Collections.emptyList();

        EventBaseSink(String name, String group, Class<T> eventType) {
            this.name = name;
            this.group = group;
            this.eventType = eventType;
        }

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

        public Class<T> getEventType() {
            return eventType;
        }

        /**
         * 订阅事件
         * @param listener 事件监听器
         */
        public synchronized void subscribe(Consumer<T> listener) {
            ArrayList<Consumer<T>> temp = new ArrayList<>(listeners);
            temp.add(listener);
            this.listeners = temp;
        }

        void dispatch(T event) {
            for (Consumer<T> listener : listeners) {
                listener.accept(event);
            }
        }
    }

    class EventEndpointImpl<T extends Serializable> implements EventEndpoint<T> {

        private String name;

        private String group;

        private Class<T> eventType;

        private volatile List<Consumer<T>> listeners = Collections.emptyList();

        EventEndpointImpl(String name, String group, Class<T> eventType) {
            this.name = name;
            this.group = group;
            this.eventType = eventType;
        }

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

        public Class<T> getEventType() {
            return eventType;
        }

        @Override
        public void fire(T event) {
            EventBus.this.fire(getName(), event);
        }

        @Override
        public synchronized void subscribe(Consumer<T> listener) {
            ArrayList<Consumer<T>> temp = new ArrayList<>(listeners);
            temp.add(listener);
            this.listeners = temp;
        }

        @SuppressWarnings("FunctionalExpressionCanBeFolded")
        @Override
        public EventSink<T> createSink() {
            return EventEndpointImpl.this::subscribe;
        }

        void dispatch(T event) {
            for (Consumer<T> listener : listeners) {
                listener.accept(event);
            }
        }
    }
}
