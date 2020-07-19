package com.demo.lock;

public interface DistributedLockManager {

    DistributedLock newLock(String name) throws DistributedLockException, InterruptedException;
}
