package com.demo.metrics;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryUsageListener {

    private final Logger logger = LoggerFactory.getLogger(MemoryUsageListener.class);

    private MemoryMXBean mbean;

    private Map<String, Usage> map = new HashMap<>();

    public MemoryUsageListener() {
        mbean = ManagementFactory.getMemoryMXBean();

        for (MemoryPoolMXBean pool :
                ManagementFactory.getMemoryPoolMXBeans()) {
            String area;
            switch (pool.getType()) {
                case HEAP:
                    area = "heap";
                    break;
                case NON_HEAP:
                    area = "nonheap";
                    break;
                default:
                    throw new IllegalStateException("未知的类型: " + pool.getType());
            }
            map.put(pool.getName(), new Usage(pool.getName(), area));
        }

        NotificationEmitter emitter = (NotificationEmitter) mbean;
        emitter.addNotificationListener((n, hb) -> {
            if (n.getType().equals(
                    MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {

//                logger.info("n={} source={}, userData={}",n,  n.getSource().getClass().getName(), n.getUserData());
                CompositeData compositeData = (CompositeData) n.getUserData();
                long count = (long) compositeData.get("count");
                String poolName = (String) compositeData.get("poolName");
                CompositeData usage = (CompositeData) compositeData.get("usage");
                long committed = (long) usage.get("committed");
                long init = (long) usage.get("init");
                long max = (long) usage.get("max");
                long used = (long) usage.get("used");
                logger.info("count={}, poolName={}, committed={}, init={}, max={}, used={}", count, poolName, committed, init, max, used);

                map.get(poolName).update(committed, init, max, used, count);
            }
        }, null, null);
    }

    public void setPercentageUsageThreshold(double percentage) {
        if (percentage <= 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException("Percentage not in range");
        }

        for (MemoryPoolMXBean pool :
                ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.isCollectionUsageThresholdSupported()) {
                long maxMemory = pool.getUsage().getMax();
                long warningThreshold = (long) (maxMemory * percentage);
                pool.setCollectionUsageThreshold(warningThreshold);
            }
        }
    }

    static class Usage {

        private AtomicLong committed;
        private AtomicLong init;
        private AtomicLong max;
        private AtomicLong used;
        private AtomicLong count;

        Usage(String name, String area) {
            Tag tag1 = new ImmutableTag("id", name);
            Tag tag2 = new ImmutableTag("area", area);
            Iterable<Tag> tags = Arrays.asList(tag1, tag2);
            committed = Metrics.gauge("cloud.jvm.memory.pool.collection.committed", tags, new AtomicLong());
            init = Metrics.gauge("cloud.jvm.memory.pool.collection.init", tags, new AtomicLong());
            max = Metrics.gauge("cloud.jvm.memory.pool.collection.max", tags, new AtomicLong());
            used = Metrics.gauge("cloud.jvm.memory.pool.collection.used", tags, new AtomicLong());
            count = Metrics.gauge("cloud.jvm.memory.pool.collection.count", tags, new AtomicLong());
        }

        void update(long committed, long init, long max, long used, long count) {
            this.committed.set(committed);
            this.init.set(init);
            this.max.set(max);
            this.used.set(used);
            this.count.set(count);
        }
    }

    public static void main(String[] args) {

        MemoryUsageListener m = new MemoryUsageListener();
        m.setPercentageUsageThreshold(0.1);

        Collection<byte[]> numbers = new LinkedList<>();
        while (true) {
            try {
                numbers.add(new byte[1024 * 1024]);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }
}
