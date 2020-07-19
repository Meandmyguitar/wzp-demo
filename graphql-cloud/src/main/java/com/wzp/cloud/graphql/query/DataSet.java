package com.wzp.cloud.graphql.query;

import com.lanmaoly.cloud.graphql.GraphQLType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@GraphQLType(name = "{1}{0}")
public class DataSet<T> {

    public static <T> DataSet<T> newDataSet(List<T> data, Long total) {
        return new DataSet<>(data, total);
    }

    private DataSet(List<T> data, Long total) {
        Objects.requireNonNull(data);
        this.total = total;
        this.data = Collections.unmodifiableList(data);
    }

    private final Long total;

    private final List<T> data;

    public Long getTotal() {
        return total;
    }

    public List<T> getData() {
        return data;
    }
}
