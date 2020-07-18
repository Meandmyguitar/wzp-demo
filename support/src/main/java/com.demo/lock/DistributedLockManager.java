package com.lanmaoly.cloud.support.lock;

public interface DistributedLockManager {

    DistributedLock newLock(String name) throws DistributedLockException, InterruptedException;
}
