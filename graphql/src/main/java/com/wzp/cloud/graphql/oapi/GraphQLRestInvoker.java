package com.wzp.cloud.graphql.oapi;

import com.wzp.cloud.graphql.Context;
import com.wzp.cloud.graphql.GraphQLExecutor;
import com.wzp.cloud.graphql.GraphQLJsons;
import com.wzp.cloud.graphql.GraphQLRequestBody;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GraphQLRestInvoker {

    private RestStore store;

    private Action action;

    private String path;

    private String rootObject;

    private String field;

    private String subField;

    public GraphQLRestInvoker(RestStore store, String path) {
        this.store = store;
        this.path = path;
        this.action = store.getActions().get(path);

        parsePath();
    }

    public CompletableFuture<String> invokeReturnJson(Map<String, Object> variable, Context context, GraphQLExecutor invoker) {
        return invoke(variable, context, invoker).thenApply(GraphQLJsons::json);
    }

    public CompletableFuture<RestResponse> invoke(Map<String, Object> variable, Context context, GraphQLExecutor invoker) {

        String gql = parse();

        String op = getOperationName(gql);

        GraphQLRequestBody requestBody = new GraphQLRequestBody();
        requestBody.setVariables(variable);
        requestBody.setOperationName(op);
        requestBody.setQuery(gql);
        CompletableFuture<ExecutionResult> result = invoker.execute(requestBody, context);
        return result.thenApply(this::transformResult);
    }

    private String getOperationName(String gql) {

        String name = "";
        Document doc = Parser.parse(gql);

        List<OperationDefinition> operationDefinitions = doc.getDefinitionsOfType(OperationDefinition.class);
        if (operationDefinitions.size() > 0) {
            name = operationDefinitions.get(0).getName();
        }
        return name;
    }

    public String parse() {

        StringBuilder sb = new StringBuilder();
        sb.append("query Temp");

        sb.append(makeOperationArguments());

        sb.append(" {");
        sb.append(makeSelectionArguments());
        sb.append(makeSelection());
        sb.append(" }");

        System.out.println(sb.toString());
        return sb.toString();
    }

    public RestResponse transformResult(ExecutionResult executionResult) {
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
        try {
            PropertyUtilsBean propertyUtils = BeanUtilsBean2.getInstance().getPropertyUtils();
            Object data = executionResult.getData();
            if (data == null) {
                return null;
            }
            data = propertyUtils.getProperty(data, field);
            if (subField != null) {
                data = propertyUtils.getProperty(data, subField);
            }
            return data;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private String makeSelectionArguments() {
        StringBuilder sb = new StringBuilder();
        sb.append(field);
        if (action.getArguments().size() == 0) {
            return sb.toString();
        }
        sb.append("(");
        boolean first;
        first = true;
        for (Argument argument : action.getArguments()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(argument.getName()).append(": $").append(argument.getName());
        }
        sb.append(") ");
        return sb.toString();
    }

    private String makeOperationArguments() {
        if (action.getArguments().size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean first = true;
        for (Argument argument : action.getArguments()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append("$").append(argument.getName()).append(": ").append(argument.getType().getName());
        }
        sb.append(")");
        return sb.toString();
    }

    private String makeSelection() {
        StringBuilder sb = new StringBuilder();
        ObjectType returnType = store.getTypes().get(action.getType().getName());
        if (returnType != null) {
            if (subField != null) {
                sb.append(" {").append(subField);
            }
            sb.append(makeSelection(returnType));
            if (subField != null) {
                sb.append(" }");
            }
        }
        return sb.toString();
    }

    private String makeSelection(ObjectType objectType) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        if (objectType.isEntity()) {
            for (Property property : objectType.getProperties()) {

                if (property.getType().getType() == PropertyTypes.Object) {
                    if (property.getName().equals(subField)) {
                        ObjectType ot = store.getTypes().get(property.getType().getName());
                        sb.append(" ").append(property.getName()).append(" ").append(makeSelection(ot));
                    } else {
                        continue;
                    }
                }
                sb.append(" ").append(property.getName()).append(" ");
            }
        } else {
            for (Property property : objectType.getProperties()) {
                if (property.getType().getType() == PropertyTypes.Object) {
                    ObjectType ot = store.getTypes().get(property.getType().getName());
                    sb.append(" ").append(property.getName()).append(" ").append(makeSelection(ot));
                } else {
                    sb.append(" ").append(property.getName()).append(" ");
                }
            }
        }
        sb.append("} ");
        return sb.toString();
    }

    private void parsePath() {
        String[] arr = path.split("/");
        if (arr.length < 2) {
            throw new IllegalArgumentException();
        }

        rootObject = arr[0];
        field = arr[1];
        subField = arr.length > 2 ? arr[2] : null;
    }


}
