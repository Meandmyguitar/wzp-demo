package com.wzp.cloud.graphql.oapi;

public class Type {

    public static Type id() {
        return new Type().type(PropertyTypes.Id).name("ID");
    }

    public static Type string() {
        return new Type().type(PropertyTypes.String).name("String");
    }

    public static Type bool() {
        return new Type().type(PropertyTypes.Boolean).name("Boolean");
    }

    public static Type integer() {
        return new Type().type(PropertyTypes.Int).name("Int");
    }

    public static Type floats() {
        return new Type().type(PropertyTypes.Float).name("Float");
    }

    public static Type object(String name) {
        return new Type().type(PropertyTypes.Object).name(name);
    }

    private String name;

    private PropertyTypes type;

    private boolean isArray;

    public Type name(String name) {
        this.name = name;
        return this;
    }

    public Type type(PropertyTypes type) {
        this.type = type;
        return this;
    }

    public Type array() {
        this.isArray = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public PropertyTypes getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }
}
