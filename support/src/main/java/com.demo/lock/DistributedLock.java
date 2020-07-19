package com.demo.lock;

public interface DistributedLock {

    /**
     * 等待加锁到成功
     */
    void lock() throws DistributedLockException, InterruptedException;

    /**
     * 解锁
     */
    void unlock() throws DistributedLockException;
}
