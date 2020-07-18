package com.lanmaoly.cloud.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lanmaoly.cloud.support.metrics")
public class MetricsProperties {

    /**
     * GC后，内存监听阈值
     */
    private double memoryCollectionUsageThreshold = 0.0001;

    public double getMemoryCollectionUsageThreshold() {
        return memoryCollectionUsageThreshold;
    }

    public void setMemoryCollectionUsageThreshold(double memoryCollectionUsageThreshold) {
        this.memoryCollectionUsageThreshold = memoryCollectionUsageThreshold;
    }
}
