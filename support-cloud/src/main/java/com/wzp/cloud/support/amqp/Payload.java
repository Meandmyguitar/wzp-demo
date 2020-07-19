package com.wzp.cloud.support.amqp;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

class Payload implements Serializable {

    private Serializable data;

    private Map<String, String> header;

    public Payload(Serializable data) {
        this(data, Collections.emptyMap());
    }

    public Payload(Serializable data, Map<String, String> header) {
        Objects.requireNonNull(header);
        this.data = data;
        this.header = header;
    }

    public Serializable getData() {
        return data;
    }

    public Map<String, String> getHeader() {
        return header;
    }
}
