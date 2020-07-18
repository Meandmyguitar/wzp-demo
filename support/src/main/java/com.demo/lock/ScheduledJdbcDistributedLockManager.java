package com.lanmaoly.cloud.support.lock;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class ScheduledJdbcDistributedLockManager implements DistributedLockManager, AutoCloseable {

    private JdbcDistributedLockManager manager;

    private ScheduledFuture<?> renewalFuture;

    private ScheduledFuture<?> cleanFuture;

    public ScheduledJdbcDistributedLockManager(
            JdbcDistributedLockManager manager,
            ScheduledExecutorService executor,
            Duration renewalInterval,
            Duration cleanInterval) {
        this.manager = manager;
        renewalFuture = executor.scheduleWithFixedDelay(this::renewal, renewalInterval.toMillis(), renewalInterval.toMillis(), TimeUnit.MILLISECONDS);
        cleanFuture = executor.scheduleWithFixedDelay(this::clean, cleanInterval.toMillis(), cleanInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    void renewal() {
        manager.renewal();
    }

    /**
     * 清理失效的记录
     */
    private void clean() {
        manager.clean();
    }

    @Override
    public void close() {
        renewalFuture.cancel(true);
        cleanFuture.cancel(true);
    }

    @Override
    public DistributedLock newLock(String name) throws DistributedLockException, InterruptedException {
        return manager.newLock(name);
    }
}
