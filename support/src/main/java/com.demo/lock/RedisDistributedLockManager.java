package com.demo.lock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisDistributedLockManager implements DistributedLockManager, AutoCloseable {

    private RedissonClient client;

    private String prefix = "##rlock##";

    public RedisDistributedLockManager(String address) {
        this(address, 0);
    }

    public RedisDistributedLockManager(String address, int database) {
        Config config = new Config();
        config.useSingleServer().setAddress(address).setDatabase(database);
        client = Redisson.create(config);
    }

    @Override
    public DistributedLock newLock(String name) throws DistributedLockException {
        return new Lock(client.getLock(prefix + name));
    }

    @Override
    public void close() {
        client.shutdown();
    }

    class Lock implements DistributedLock {

        private RLock lock;

        Lock(RLock lock) {
            this.lock = lock;
        }

        @Override
        public void lock() throws DistributedLockException, InterruptedException {
            lock.lockInterruptibly();
        }

        @Override
        public void unlock() throws DistributedLockException {
            lock.unlock();
        }
    }
}
