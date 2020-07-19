package com.wzp.cloud.graphql.oapi;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;

public class GraphQLUtils {

    /**
     * 忽略非空，数组等包装类型。获取实际的类型
     */
    public static GraphQLType unwrap(GraphQLType type) {
        if (type instanceof GraphQLNonNull) {
            return unwrap(((GraphQLNonNull) type).getWrappedType());
        } else if (type instanceof GraphQLList) {
            return unwrap(((GraphQLList) type).getWrappedType());
        } else {
            return type;
        }
    }
}
