package com.wzp.cloud.support.lock;

@SuppressWarnings("WeakerAccess")
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
