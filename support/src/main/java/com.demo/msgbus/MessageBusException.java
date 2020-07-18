package com.lanmaoly.cloud.support.msgbus;

public class MessageBusException extends RuntimeException {

    public MessageBusException() {
    }

    public MessageBusException(String message) {
        super(message);
    }

    public MessageBusException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageBusException(Throwable cause) {
        super(cause);
    }

    public MessageBusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
