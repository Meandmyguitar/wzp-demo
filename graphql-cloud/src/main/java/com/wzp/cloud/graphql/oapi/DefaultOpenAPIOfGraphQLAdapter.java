package com.wzp.cloud.graphql.oapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanmaoly.cloud.graphql.Context;
import com.lanmaoly.cloud.graphql.GraphQLExecutor;
import com.lanmaoly.cloud.graphql.GraphQLRequestBody;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import io.swagger.v3.core.util.Json;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultOpenAPIOfGraphQLAdapter implements GraphQLOpenAPIAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOpenAPIOfGraphQLAdapter.class);

    public static final String PREFIX = "/graphql/openapi/";

    private final GraphQLExecutor executor;

    private final GraphQLQueryRegistry registry;

    private final String openAPIJson;

    private final ObjectMapper mapper = new ObjectMapper();

    public DefaultOpenAPIOfGraphQLAdapter(
            GraphQLSchema schema,
            GraphQLExecutor executor,
            ResourcePatternResolver patternResolver) throws IOException {
        this.executor = executor;
        this.registry = new GraphQLQueryRegistry(schema);

        try {
            for (Resource resource : patternResolver.getResources("classpath:graphql/openapi/**/*.graphqls")) {
                Document document = new Parser().parseDocument(IOUtils.toString(resource.getURL(), StandardCharsets.UTF_8));
                List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
                for (OperationDefinition operation : operations) {
                    String name = PREFIX + operation.getOperation().name().toLowerCase() + "/" + operation.getName();
                    registry.add(name, document, operation.getName());
                    logger.info("添加openapi接口{}", name);
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
        }
        openAPIJson = Json.pretty(registry.openApi());
    }

    public String getOpenAPIJson() {
        return openAPIJson;
    }

    @Override
    public CompletableFuture<RestResponse> execute(String path, String variableJson, Context context) {
        try {
            GraphQLQueryRegistry.Entry entry = registry.get(path);
            Map<String, Object> variables = mapper.readValue(variableJson, Map.class);
            GraphQLRequestBody requestBody = new GraphQLRequestBody();
            requestBody.setQuery(entry.getQuery());
            requestBody.setVariables(variables);
            requestBody.setOperationName(entry.getOperation());
            return executor.execute(requestBody, context).thenApply(r -> this.transformResult(entry, r));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private RestResponse transformResult(GraphQLQueryRegistry.Entry entry, ExecutionResult executionResult) {
        String message = null;
        String errorCode = "0";
        if (executionResult.getErrors().size() > 0) {
            GraphQLError error = executionResult.getErrors().get(0);
            message = error.getMessage();
            if (error.getExtensions() == null) {
                errorCode = "1";
            } else {
                errorCode = (String) error.getExtensions().getOrDefault("code", "1");
            }
        }
        Object data = transformData(executionResult);

        return new RestResponse(errorCode, message, data);
    }

    private Object transformData(ExecutionResult executionResult) {
//        PropertyUtilsBean propertyUtils = BeanUtilsBean2.getInstance().getPropertyUtils();
        Object data = executionResult.getData();
        if (data == null) {
            return null;
        }
        return data;
    }
}
