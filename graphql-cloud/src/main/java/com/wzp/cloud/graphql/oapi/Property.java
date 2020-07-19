package com.wzp.cloud.graphql.oapi;

public class Property {

    private String name;

    private Type type;

    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public boolean isID() {
        return type.getType() == PropertyTypes.Id;
    }

    public Property name(String name) {
        this.name = name;
        return this;
    }

    public Property description(String description) {
        this.description = description;
        return this;
    }

    public Property type(Type type) {
        this.type = type;
        return this;
    }
}
