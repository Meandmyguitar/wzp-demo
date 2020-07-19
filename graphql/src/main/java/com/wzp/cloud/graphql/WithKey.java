package com.wzp.cloud.graphql;

public class WithKey<K, V> {

    public static <K, V> WithKey<K, V> of(K key, V data) {
        return new WithKey<>(key, data);
    }

    public WithKey(K key, V data) {
        this.data = data;
        this.key = key;
    }

    private final K key;

    private final V data;

    public K getKey() {
        return key;
    }

    public V getData() {
        return data;
    }
}
