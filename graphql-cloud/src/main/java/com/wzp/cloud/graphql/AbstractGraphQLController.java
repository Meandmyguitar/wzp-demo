package com.wzp.cloud.graphql;

import java.util.concurrent.CompletableFuture;


public abstract class AbstractGraphQLController {

    private final GraphQLExecutor executor;

    public AbstractGraphQLController(GraphQLExecutor executor) {
        this.executor = executor;
    }

    public CompletableFuture<String> invoke(GraphQLRequestBody request,
                                            Context context) {
        return executor.execute(request, context).thenApply(GraphQLJsons::json);
    }
}
