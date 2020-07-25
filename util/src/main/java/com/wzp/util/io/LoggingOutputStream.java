
package com.wzp.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class LoggingOutputStream extends OutputStream {
    private OutputStream output;
    private int remain;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public LoggingOutputStream(OutputStream output, int remain) {
        if (remain < 0) {
            throw new IllegalArgumentException("remain 不能小于0");
        } else {
            Objects.requireNonNull(output);
            this.output = output;
            this.remain = remain;
        }
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public void write(int b) throws IOException {
        byte bb = (byte)(b & 255);
        byte[] array = new byte[]{bb};
        this.write(array, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.remain > 0) {
            int w = Math.min(this.remain, len);
            this.buffer.write(b, off, w);
            this.remain -= w;
        }

        this.output.write(b, off, len);
    }
}
