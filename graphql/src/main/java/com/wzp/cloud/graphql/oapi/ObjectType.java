package com.wzp.cloud.graphql.oapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObjectType {

    private String name;

    private boolean isEnum;

    private List<Property> properties = new ArrayList<>();

    private String description;

    public String getName() {
        return name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isEntity() {
        return getIdProperty().isPresent();
    }

    public ObjectType name(String name) {
        this.name = name;
        return this;
    }

    public ObjectType description(String description) {
        this.description = description;
        return this;
    }

    public ObjectType enumType() {
        this.isEnum = true;
        return this;
    }

    public ObjectType properties(List<Property> properties) {
        this.properties = properties;
        return this;
    }

    public ObjectType add(Property property) {
        properties.add(property);
        return this;
    }

    public Optional<Property> getIdProperty() {
        return getProperties().stream().filter(Property::isID).findFirst();
    }
}
