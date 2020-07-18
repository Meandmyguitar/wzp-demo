package com.lanmaoly.cloud.support.lock;

import java.util.concurrent.locks.ReentrantLock;

class ReentrantDistributedLock implements DistributedLock {

    private ReentrantLock lock = new ReentrantLock();

    private DistributedLock coreLock;

    ReentrantDistributedLock(DistributedLock coreLock) {
        this.coreLock = coreLock;
    }

    @Override
    public void lock() throws InterruptedException {
        if (lock.isHeldByCurrentThread()) {
            lock.lock();
            return;
        }

        lock.lock();
        try {
            coreLock.lock();
        } catch (Throwable e) {
            lock.unlock();
            throw e;
        }
    }

    @Override
    public void unlock() {
        if (lock.getHoldCount() == 1) {
            try {
                coreLock.unlock();
            } finally {
                lock.unlock();
            }
        } else {
            lock.unlock();
        }
    }
}
