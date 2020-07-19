package com.wzp.cloud.graphql;

import com.wzp.cloud.graphql.query.Query;
import org.dataloader.MappedBatchLoader;

import java.util.Set;
import java.util.function.Supplier;

public interface GraphQLConfigurer {

    GraphQLConfigurer schema(String... paths);

    GraphQLConfigurer errorHandler(ErrorHandler handler);

    DataLoaderBuilder loader();

    WiringBuilder wiring();

    interface DataLoaderBuilder {

        <K, V> DataLoaderBuilder with(String name, Supplier<MappedBatchLoader<K, V>> supplier);

        <K, V> DataLoaderBuilder with(String name, FlatBatchLoader<K, V> fetcher, boolean isArray);

        <K> DataLoaderBuilder with(String name, Query<Set<K>> query, boolean isArray);
    }

    interface WiringBuilder {

        WiringBuilder root(Object query);

        WiringBuilder type(Class<?> clazz);

        WiringBuilder context(Object context);
    }
}
