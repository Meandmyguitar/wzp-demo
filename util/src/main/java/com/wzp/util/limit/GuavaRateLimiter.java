package com.wzp.util.limit;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流
 */
public class GuavaRateLimiter {

    public static final ConcurrentHashMap<String, RateLimiter> resourceRateLimiter = new ConcurrentHashMap();

    static {
        createResourceLimiter("order", 10);
    }

    private static void createResourceLimiter(String resource, int qps) {
        if (resourceRateLimiter.contains(resource)) {
            resourceRateLimiter.get(resource).setRate(qps);
        } else {
            RateLimiter rateLimiter = RateLimiter.create(qps);
            resourceRateLimiter.putIfAbsent(resource, rateLimiter);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5000; i++) {
            new Thread(() -> {
                if (resourceRateLimiter.get("order").tryAcquire(10, TimeUnit.MILLISECONDS)) {
                    System.out.println("执行正常业务逻辑");
                } else {
//                    System.out.println("限流");
                }
            }).start();
        }
    }
}
