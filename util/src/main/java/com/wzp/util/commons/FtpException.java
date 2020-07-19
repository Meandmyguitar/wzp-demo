package com.wzp.util.commons;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class FtpException extends IOException {

    private int code;

    public FtpException(int code) {
        this.code = code;
    }

    public FtpException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
