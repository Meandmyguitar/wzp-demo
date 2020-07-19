package com.wzp.cloud.graphql;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertNotNull;

public class DefaultGraphQLError implements GraphQLError {

    private final String message;
    private final List<Object> path;
    private final List<SourceLocation> locations;
    private final Map<String, Object> extensions;

    public DefaultGraphQLError(ExecutionPath path, String message, SourceLocation sourceLocation, String errorCode) {
        this.path = assertNotNull(path).toList();
        this.locations = Collections.singletonList(sourceLocation);
        this.extensions = mkExtensions(errorCode);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    private Map<String, Object> mkExtensions(String errorCode) {
        Map<String, Object> extensions = new LinkedHashMap<>();
        extensions.put("code", errorCode);
        return extensions;
    }
}
