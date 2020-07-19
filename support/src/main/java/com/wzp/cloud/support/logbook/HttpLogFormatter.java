package com.wzp.cloud.support.logbook;

import org.zalando.logbook.PreparedHttpLogFormatter;

import java.util.Map;

public class HttpLogFormatter implements PreparedHttpLogFormatter {

    @SuppressWarnings("NullableProblems")
    @Override
    public String format(Map<String, Object> content) {
        StringBuilder sb = new StringBuilder();
        sb.append("type=").append(content.get("type"));
        if (content.containsKey("uri")) {
            sb.append(", ").append("uri=").append(content.get("uri"));
        }
        if (content.containsKey("status")) {
            sb.append(", ").append("status=").append(content.get("status"));
        }
        if (content.containsKey("method")) {
            sb.append(", ").append("method=").append(content.get("method"));
        }
        if (content.containsKey("remote")) {
            sb.append(", ").append("remote=").append(content.get("remote"));
        }
        if (content.containsKey("duration")) {
            sb.append(", ").append("duration=").append(content.get("duration"));
        }
        if (content.containsKey("body")) {
            sb.append(", ").append("body=").append(content.get("body"));
        }
        return sb.toString();
    }
}
