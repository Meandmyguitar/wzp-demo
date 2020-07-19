package com.wzp.cloud.graphql.oapi;

import graphql.schema.GraphQLFieldDefinition;

import java.util.ArrayList;
import java.util.List;

public class Action {

    private String name;

    private String description;

    private List<Argument> arguments = new ArrayList<>();

    private Type type;

    private GraphQLFieldDefinition gqlField;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public Type getType() {
        return type;
    }

    public Action name(String name) {
        this.name = name;
        return this;
    }

    public Action description(String description) {
        this.description = description;
        return this;
    }

    public Action type(Type type) {
        this.type = type;
        return this;
    }

    public Action gqlField(GraphQLFieldDefinition field) {
        this.gqlField = field;
        return this;
    }

    public Action add(Argument argument) {
        arguments.add(argument);
        return this;
    }
}
