package com.wzp.cloud.graphql.oapi;

import com.wzp.cloud.graphql.Context;

import java.util.concurrent.CompletableFuture;

public interface GraphQLOpenAPIAdapter {

    String getOpenAPIJson();

    CompletableFuture<RestResponse> execute(String path, String variableJson, Context context);
}
