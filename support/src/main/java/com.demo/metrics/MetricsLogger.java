package com.demo.metrics;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.Collections;

/**
 * 给业务记录提供一些标准的Metrics记录方法
 */
public class MetricsLogger {

    /**
     * 记录异常出现次数
     */
    public static void exceptionCount(Throwable throwable) {
        exceptionCount("cloud.custom.exceptions", throwable);
    }

    /**
     * 记录异常出现次数
     */
    public static void exceptionCount(String name, Throwable throwable) {
        Tag tag = new ImmutableTag("type", throwable.getClass().getName());
        Metrics.counter(name, Collections.singletonList(tag)).increment();
    }
}
