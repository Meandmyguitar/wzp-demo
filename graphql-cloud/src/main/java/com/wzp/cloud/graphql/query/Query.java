package com.wzp.cloud.graphql.query;

public interface Query<F> {

    <T> DataSet<T> execute(QueryContext<F> context);
}
