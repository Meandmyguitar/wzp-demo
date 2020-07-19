package com.wzp.cloud.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class GraphQLJsons {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String json(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseVariable(String json) {
        try {
            return mapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
