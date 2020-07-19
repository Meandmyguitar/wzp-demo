package com.wzp.cloud.graphql;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface FlatBatchLoader<K, V> extends Function<Set<K>, List<WithKey<K, V>>> {
}
