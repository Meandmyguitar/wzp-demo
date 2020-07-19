package com.wzp.cloud.graphql.oapi;

public class Argument {

    private String name;

    private Type type;

    private boolean isArray;

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public Argument name(String name) {
        this.name = name;
        return this;
    }

    public Argument type(Type type) {
        this.type = type;
        return this;
    }

    public Argument array() {
        this.isArray = true;
        return this;
    }
}
