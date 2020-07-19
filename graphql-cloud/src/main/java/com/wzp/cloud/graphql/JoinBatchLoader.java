package com.wzp.cloud.graphql;

import com.lanmaoly.cloud.graphql.query.DataSet;
import com.lanmaoly.cloud.graphql.query.Query;
import com.lanmaoly.cloud.graphql.query.QueryContext;
import com.lanmaoly.cloud.graphql.query.QueryOption;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.dataloader.MappedBatchLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class JoinBatchLoader<K, V> implements MappedBatchLoader<K, V> {

    private final Query<Set<K>> query;

    private final boolean isArray;

    public JoinBatchLoader(Query<Set<K>> query, boolean isArray) {
        this.query = query;
        this.isArray = isArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Map<K, V>> load(Set<K> keys) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryOption option = new QueryOption(0L, null, null, false);
                DataSet<?> dataSet = query.execute(QueryContext.of(keys, option));

                Map<K, List<?>> groupByKeys = groupByKeys(dataSet.getData(), this::keyOf, v -> {
                    if (v instanceof WithKey) {
                        return ((WithKey<K, V>) v).getData();
                    } else {
                        return v;
                    }
                });
                if (!isArray) {
                    HashMap<K, V> newMap = new HashMap<>();
                    groupByKeys.forEach((k, v) -> newMap.put(k, v.size() == 0 ? null : (V)v.get(0)));
                    return newMap;
                } else {
                    return (Map<K, V>) groupByKeys;
                }
            } catch (Exception e) {
                throw new IllegalStateException("执行查询时出错: " + query, e);
            }
        });
    }


    @SuppressWarnings("unchecked")
    private Map<K, List<?>> groupByKeys(List<?> list, Function<Object, K> keyGetter, Function<Object, ?> valueGetter) {
        Map<K, List<?>> map = new LinkedHashMap<>();
        list.forEach(item -> {
            K key = keyGetter.apply(item);
            List<Object> v = (List<Object>) map.computeIfAbsent(key, s -> new ArrayList<>());
            v.add(valueGetter.apply(item));
        });
        return map;
    }


    @SuppressWarnings("unchecked")
    private K keyOf(Object v) {
        if (v instanceof WithKey) {
            return ((WithKey<K, V>) v).getKey();
        }
        Object key = readProperty(v, "key");
        if (key == null) {
            throw new IllegalStateException("获取对象的key失败,值不能为null");
        }
        return (K) key;
    }

    private Object readProperty(Object v, String name) {
        Objects.requireNonNull(v);
        Objects.requireNonNull(name);
        try {
            return BeanUtilsBean2.getInstance().getPropertyUtils().getProperty(v, name);
//            return BeanUtilsBean2.getInstance().getProperty(v, name);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("获取对象的属性失败: " + name, e);
        }
    }
}
