package com.wzp.cloud.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lanmaoly.cloud.support.shared")
public class SharedProperties {

    /**
     * 共享的线程池
     */
    private int scheduledThreadPool = 5;

    public int getScheduledThreadPool() {
        return scheduledThreadPool;
    }

    public void setScheduledThreadPool(int scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }
}
