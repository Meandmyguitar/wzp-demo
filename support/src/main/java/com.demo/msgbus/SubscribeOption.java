package com.demo.msgbus;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public final class SubscribeOption {

    private final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    public static SubscribeOption of(String group, String topic) {
        return new SubscribeOption().group(group).topic(topic);
    }

    private String group;

    private String topic;

    private SubscribeOption() {
    }

    public String getGroup() {
        return group;
    }

    public String getTopic() {
        return topic;
    }

    public SubscribeOption group(String group) {
        Objects.requireNonNull(group);
        if (!PATTERN.matcher(group).matches()) {
            throw new IllegalArgumentException("格式不正确");
        }

        SubscribeOption option = copy();
        option.group = group;
        return option;
    }

    public SubscribeOption topic(String topic) {
        Objects.requireNonNull(topic);
        if (!PATTERN.matcher(topic).matches()) {
            throw new IllegalArgumentException("格式不正确");
        }

        SubscribeOption option = copy();
        option.topic = topic;
        return option;
    }

    protected SubscribeOption copy() {
        SubscribeOption option = new SubscribeOption();
        option.group = group;
        option.topic = topic;
        return option;
    }
}
