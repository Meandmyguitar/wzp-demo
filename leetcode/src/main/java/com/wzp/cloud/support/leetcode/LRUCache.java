package com.wzp.cloud.support.leetcode;

import java.util.LinkedHashMap;
import java.util.Map;

class LRUCache<K, V> extends LinkedHashMap<K, V> {

    public final int cache_size;

    LRUCache(int size) {
        super((int) Math.ceil(size / 0.75) + 1, 0.75f, true);
        this.cache_size = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cache_size;
    }

    public static void main(String[] args) {
        System.out.println((int) Math.ceil(3 / 0.75) + 1);
        System.out.println(5 * 0.75);
        LRUCache<Integer, String> luaCache = new LRUCache<>(3);

        for (int i = 0; i < 5; i++) {
            String put = luaCache.put(i, i + "");
            System.out.println("插入" + i + ",put=" + put + ",map value :" + luaCache);
        }
    }

}