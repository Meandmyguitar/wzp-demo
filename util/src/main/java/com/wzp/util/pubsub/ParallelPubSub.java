package com.wzp.util.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("WeakerAccess")
public class ParallelPubSub implements PubSub, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(ParallelPubSub.class);

    private final Map<String, List<Listener<Object>>> listeners = new HashMap<>();

    private final Map<String, List<Listener<Object>>> syncListeners = new HashMap<>();

    // 处理消息的专用scheduler
    private Scheduler scheduler;

    private CountDownLatch latch;

    private FluxSink<Envelop> sink;

    private AtomicLong statConsumeCount = new AtomicLong();

    private AtomicLong statPublishCount = new AtomicLong();

    public ParallelPubSub(int parallelism) {

        latch = new CountDownLatch(parallelism);
        scheduler = Schedulers.newParallel("event", parallelism);

        // IMPORTANT！ 发布消息和处理消息使用隔离的scheduler，确保他们不在同一个线程里
        Flux.<Envelop>create(emitter -> this.sink = emitter).publishOn(Schedulers.parallel()).parallel(parallelism)
                .runOn(scheduler).subscribe(this::handleEnvelop, ex -> {
            logger.error(ex.getMessage(), ex);
            latch.countDown();
        }, () -> latch.countDown());
    }

    public long getStatConsumeCount() {
        return statConsumeCount.get();
    }

    public long getStatPublishCount() {
        return statPublishCount.get();
    }

    @Override
    public void close() throws InterruptedException {
        try {
            this.sink.complete();
            latch.await();
        } finally {
            scheduler.dispose();
        }
    }

    @Override
    public void publishAsync(String topic, Object message) {
        if (logger.isDebugEnabled()) {
            logger.debug("publishAsync,topic: {} ,message: {}", topic, message);
        }

        this.sink.next(new Envelop(topic, message));
        this.statPublishCount.incrementAndGet();
    }

    @Override
    public void publishSync(String topic, Object message) {
        fireSync(topic, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(String topic, Listener listener) {
        synchronized (listeners) {
            List<Listener<Object>> list = listeners.computeIfAbsent(topic, k -> new ArrayList<>());
            list.add(listener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribeSync(String topic, Listener listener) {
        synchronized (listeners) {
            List<Listener<Object>> list = syncListeners.computeIfAbsent(topic, k -> new ArrayList<>());
            list.add(listener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Event<T> defineEvent(String topic, Class<T> eventType) {
        return new Event(this, topic);
    }

    @SuppressWarnings("unchecked")
    private void handleEnvelop(Envelop envelop) {
        statConsumeCount.incrementAndGet();
        if (logger.isDebugEnabled()) {
            logger.debug("consume topic: {} ,message: {}", envelop.getTopic(), envelop.getMessage());
        }
        getListeners(envelop.getTopic()).forEach(listener -> {
            try {
                listener.onMessage(envelop.getTopic(), envelop.getMessage());
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        });
    }

    /**
     * 触发同步事件
     */
    private void fireSync(String topic, Object message) {
        getSyncListeners(topic).forEach(listener -> {
            try {
                listener.onMessage(topic, message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Listener> getListeners(String topic) {
        List<Listener> copy = Collections.emptyList();
        synchronized (listeners) {
            List<Listener<Object>> list = listeners.get(topic);
            if (list != null) {
                copy = new ArrayList<>(list);
            }
        }
        return copy;
    }

    private List<Listener<Object>> getSyncListeners(String topic) {
        List<Listener<Object>> copy = Collections.emptyList();
        synchronized (syncListeners) {
            List<Listener<Object>> list = syncListeners.get(topic);
            if (list != null) {
                copy = new ArrayList<>(list);
            }
        }
        return copy;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Envelop {

        private String topic;

        private Object message;

        public Envelop(String topic, Object message) {
            this.topic = topic;
            this.message = message;
        }

        public String getTopic() {
            return topic;
        }

        public Object getMessage() {
            return message;
        }
    }
}
