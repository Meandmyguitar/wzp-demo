package com.wzp.cloud.graphql.oapi;

class RestResponse {

    private String errorCode;

    private String message;

    private Object data;

    public RestResponse(String errorCode, String message, Object data) {
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
