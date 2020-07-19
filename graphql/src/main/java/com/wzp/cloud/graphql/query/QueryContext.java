package com.wzp.cloud.graphql.query;

public class QueryContext<T> {

    public static <T> QueryContext<T> of(T filters, QueryOption option) {
        return new QueryContext<>(filters, option);
    }

    private final T filters;

    private final QueryOption option;

    public QueryContext(T filters, QueryOption option) {
        this.filters = filters;
        this.option = option;
    }

    public T getFilters() {
        return filters;
    }

    public QueryOption getOption() {
        return option;
    }
}
