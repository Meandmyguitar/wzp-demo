
package com.wzp.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class LoggingInputStream extends InputStream {
    private int remain;
    private InputStream input;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public LoggingInputStream(InputStream input, int remain) {
        if (remain < 0) {
            throw new IllegalArgumentException("remain 不能小于0");
        } else {
            Objects.requireNonNull(input);
            this.remain = remain;
            this.input = input;
        }
    }

    byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public int read() throws IOException {
        int d = this.input.read();
        if (d >= 0 && this.remain > 0) {
            this.buffer.write(d);
            --this.remain;
        }

        return d;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int r = this.input.read(b, off, len);
        if (r > 0) {
            int w = Math.min(r, this.remain);
            if (w > 0) {
                this.buffer.write(b, off, w);
                this.remain -= w;
            }
        }

        return r;
    }
}
