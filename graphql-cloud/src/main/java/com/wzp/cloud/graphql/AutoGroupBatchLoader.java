package com.wzp.cloud.graphql;

import org.dataloader.MappedBatchLoader;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class AutoGroupBatchLoader<K, V> implements MappedBatchLoader<K, V> {

    private final Function<Set<K>, List<WithKey<K, V>>> fetcher;

    private final boolean isArray;

    public AutoGroupBatchLoader(Function<Set<K>, List<WithKey<K, V>>> fetcher, boolean isArray) {
        this.fetcher = fetcher;
        this.isArray = isArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Map<K, V>> load(Set<K> keys) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<WithKey<K, V>> dataSet = fetcher.apply(keys);

                Map<K, List<?>> groupByKeys = groupByKeys(dataSet);
                if (!isArray) {
                    HashMap<K, V> newMap = new HashMap<>();
                    groupByKeys.forEach((k, v) -> newMap.put(k, v.size() == 0 ? null : (V) v.get(0)));
                    return newMap;
                } else {
                    return (Map<K, V>) groupByKeys;
                }
            } catch (Exception e) {
                throw new IllegalStateException("执行查询时出错: " + fetcher, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<K, List<?>> groupByKeys(List<WithKey<K, V>> list) {
        Map<K, List<?>> map = new LinkedHashMap<>();
        list.forEach(item -> {
            K key = item.getKey();
            List<Object> v = (List<Object>) map.computeIfAbsent(key, s -> new ArrayList<>());
            v.add(item.getData());
        });
        return map;
    }
}
