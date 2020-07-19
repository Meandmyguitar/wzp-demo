package com.wzp.cloud.graphql;

import java.util.HashMap;
import java.util.Objects;

public final class Context {

    public static Builder builder() {
        return new Builder();
    }

    private final Authentication authentication;

    private final HashMap<String, Object> map;

    Context(Authentication authentication, HashMap<String, Object> map) {
        this.authentication = authentication;
        this.map = map;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map;
    }

    public static class Builder {

        private Authentication authentication;

        private HashMap<String, Object> map = new HashMap<>();

        public Builder authorization(Authentication authentication) {
            Objects.requireNonNull(authentication);
            this.authentication = authentication;
            return this;
        }

        public Builder of(String key, Object value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            map.put(key, value);
            return this;
        }

        public Context build() {
            return new Context(authentication, map);
        }
    }
}
