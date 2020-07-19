package com.wzp.cloud.graphql.query;

public interface JpaQueryTransform<T> extends JpaQuery<T> {

    T transform(Object v);
}
