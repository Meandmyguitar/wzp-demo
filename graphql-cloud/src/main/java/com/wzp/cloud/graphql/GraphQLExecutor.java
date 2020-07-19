package com.wzp.cloud.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLContext;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;


public class GraphQLExecutor {

    private final DefaultGraphQLConfigurer graphQLConfigurer;

    public GraphQLExecutor(DefaultGraphQLConfigurer graphQLConfigurer) {
        this.graphQLConfigurer = graphQLConfigurer;
    }

    public CompletableFuture<ExecutionResult> execute(GraphQLRequestBody request,
                                                      Context context) {

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .context(makeContext(context))
                .operationName(request.getOperationName())
                .variables(request.getVariables() == null ? Collections.emptyMap() : request.getVariables())
                .dataLoaderRegistry(graphQLConfigurer.createDataLoaderRegistry())
                .build();
        return graphQLConfigurer.getGraphQL().executeAsync(executionInput);
    }

    private GraphQLContext makeContext(Context context) {
        GraphQLContext.Builder builder = GraphQLContext.newContext();
        if (context.getAuthentication() != null) {
            builder.of("authorization", context.getAuthentication());
        }
        builder.of("context", context);
        builder.of("configurer", graphQLConfigurer);
        return builder.build();
    }

}
