package com.wzp.cloud.graphql;

import graphql.GraphQLException;

public class NotLoginException extends GraphQLException {
    public NotLoginException() {
    }

    public NotLoginException(String message) {
        super(message);
    }

    public NotLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotLoginException(Throwable cause) {
        super(cause);
    }
}
