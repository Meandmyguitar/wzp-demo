package com.wzp.cloud.graphql;

public interface ErrorHandler {

    HandledError handleError(Throwable throwable);

    class HandledError {

        private final String errorCode;

        private final String message;

        public HandledError(String errorCode) {
            this(errorCode, null);
        }

        public HandledError(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }
    }
}
