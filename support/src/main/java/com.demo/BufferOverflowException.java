package com.demo;

public class BufferOverflowException extends RuntimeException {
    public BufferOverflowException() {
    }

    public BufferOverflowException(String message) {
        super(message);
    }

    public BufferOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public BufferOverflowException(Throwable cause) {
        super(cause);
    }

    public BufferOverflowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
