package com.wzp.cloud.graphql.query;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;

public interface JpaQuery<F> {

    void select(JPAQuery<?> query, F filters);

    ComparableExpressionBase<?> columnMapping(String column);

}
