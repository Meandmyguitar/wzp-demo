package com.wzp.cloud.support.lock;

import com.peppa.common.redis.connection.RedisConnection;
import com.peppa.common.redis.connection.command.AbstractSyncCommand;
import com.peppa.common.redis.connection.command.ExtLettuceLockCommands;
import com.peppa.common.redis.connection.command.LettuceLockCommands;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * 1. 存在弊端-如果当前加锁的业务没有处理完-redis-key-自动过期-后续业务会拿到锁
 * 2. 使用请慎重考察
 *
 * @author wangzhengpeng
 */
@Component
public class RedisDistributedLockManager1 {

    public static final Logger log = LoggerFactory.getLogger(RedisDistributedLockManager.class);

    @Value("${default_try_lock_time:1000}")
    private int defaultTryLockTimeout;

    /**
     * 补充锁过期时间阀 单位毫秒
     */
    private static final long TIME_SECONDS_FIVE = 5000;

    /**
     * 每个key的过期时间 {@link Long 过期时间}
     */
    private Map<String, Long> lockContentMap = new ConcurrentHashMap<>(512);

    private final ExtLettuceLockCommands extLettuceLockCommands;
    private final RedisConnection<String, Object> redisConnectionCustom;

    public RedisDistributedLockManager(ExtLettuceLockCommands extLettuceLockCommands, RedisConnection<String, Object> redisConnectionCustom) {
        this.extLettuceLockCommands = extLettuceLockCommands;
        this.redisConnectionCustom = redisConnectionCustom;
        ScheduleTask task = new ScheduleTask(this, lockContentMap);
        new Timer("Lock-supplement-Task").schedule(task, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key redis-key
     */
    public boolean tryLock(String key, long expireTime) {
        long start = System.currentTimeMillis();
        do {
            if (extLettuceLockCommands.lock(key, key, expireTime)) {
                lockContentMap.put(key, System.currentTimeMillis());
                return true;
            }
            try {
                randomSleep(100, 200);
            } catch (InterruptedException e) {
                return false;
            }
        } while (System.currentTimeMillis() - start <= defaultTryLockTimeout);
        return false;
    }

    /**
     * 删除分布式锁
     *
     * @param key redis-key
     */
    public Boolean unLock(String key) {
        return extLettuceLockCommands.unLock(key, key);
    }

    /**
     * 补充锁过期时间
     */
    private Boolean supplementLock(String key, String value, long time) {

        try {
            RedisCommands<String, Object> redisCommands = redisConnectionCustom.getConnection().sync();
            String lua = "if redis.call('get', KEYS[1]) == KEYS[2] then redis.call('expire', KEYS[1], KEYS[3]) return 1 else return 0 end";
            String[] arrStr = new String[]{key, value, String.valueOf(time)};
            return redisCommands.eval(lua, ScriptOutputType.BOOLEAN, arrStr, new Object[]{value});
        } catch (Exception e) {
            log.error("supplementLock hash e:{}", e);
        }
        return false;
    }

    /**
     * 随机睡眠
     */
    static void randomSleep(int min, int max) throws InterruptedException {
        int i = new Random().nextInt(max - min);
        Thread.sleep(min + i);
    }

    /**
     * 线程池异步续约过期时间
     */
    static class ScheduleTask extends TimerTask {

        private static ThreadPoolExecutor threadPool;

        static {
            threadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 200, 60,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(2000));
            log.info("ScheduleTask 初始化线程池成功...");
        }

        private final RedisDistributedLockManager redisDistributionLock;
        private final Map<String, Long> lockContentMap;

        public ScheduleTask(RedisDistributedLockManager redisDistributionLock, Map<String, Long> lockContentMap) {
            this.redisDistributionLock = redisDistributionLock;
            this.lockContentMap = lockContentMap;
        }

        @Override
        public void run() {
            if (lockContentMap.isEmpty()) {
                return;
            }
            Set<Map.Entry<String, Long>> entries = lockContentMap.entrySet();
            for (Map.Entry<String, Long> entry : entries) {
                if (entry.getValue() - System.currentTimeMillis() >= TIME_SECONDS_FIVE) {
                    //过期时间与当前时间差值大于TIME_SECONDS_FIVE数值 不需要补偿
                    continue;
                }
                threadPool.submit(() -> {
                    long time = entry.getValue() + TIME_SECONDS_FIVE;
                    log.info("supplementLock key={},time={}", entry.getKey(), time);
                    if (redisDistributionLock.supplementLock(entry.getKey(), entry.getKey(), time)) {
                        entry.setValue(time);
                    } else {
                        lockContentMap.remove(entry.getKey());
                    }
                });
            }
        }
    }
}
