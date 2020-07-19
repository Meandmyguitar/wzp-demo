package com.wzp.cloud.graphql.oapi;

import com.wzp.cloud.graphql.Context;
import com.wzp.cloud.graphql.GraphQLExecutor;
import com.wzp.cloud.graphql.GraphQLJsons;
import graphql.schema.GraphQLSchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OpenAPIAdapter {

    private GraphQLExecutor executor;

    private RestStore store;

    private OpenAPI openAPI;

    private String openAPIJson;

    public OpenAPIAdapter(GraphQLSchema schema, GraphQLExecutor executor) {
        this.executor = executor;
        store = new GraphQLSchemaParser().parse(schema);
        openAPI = new OpenAPIGenerator(store).build();
        openAPIJson = Json.pretty(openAPI);
    }

    public RestStore getStore() {
        return store;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public String getOpenAPIJson() {
        return openAPIJson;
    }

    public CompletableFuture<RestResponse> invoke(String path, String variableJson, Context context) {
        Map<String, Object> variable;
        if (StringUtils.isBlank(variableJson)) {
            variable = Collections.emptyMap();
        } else {
            variable = GraphQLJsons.parseVariable(variableJson);
        }
        return new GraphQLRestInvoker(store, path).invoke(variable, context, executor);
    }
}
