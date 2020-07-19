package com.wzp.cloud.graphql.oapi;

import graphql.schema.*;

import java.util.*;

public class GraphQLSchemaParser {

    public Map<String, ObjectType> types = new HashMap<>();

    public Map<String, Action> actions = new HashMap<>();

    public RestStore parse(GraphQLSchema schema) {
        parseTypes(schema);
        List<Action> actions = buildActions(schema);
        return new RestStore(types.values(), actions);
    }

    public List<Action> buildActions(GraphQLSchema schema) {
        List<Action> queryActions = buildAction(schema.getQueryType());
        List<Action> mutationActions = buildAction(schema.getMutationType());

        ArrayList<Action> subActions = new ArrayList<>();
        for (Action value : queryActions) {
            subActions.addAll(buildSubAction(value, schema));
        }

        List<Action> result = new ArrayList<>();
        result.addAll(queryActions);
        result.addAll(mutationActions);
        result.addAll(subActions);
        return result;
    }

    private List<Action> buildAction(GraphQLObjectType objectType) {
        List<Action> list = new ArrayList<>();
        for (GraphQLFieldDefinition field : objectType.getFieldDefinitions()) {
            Action action = new Action().name(objectType.getName().toLowerCase() + "/" + field.getName());
            action.description(field.getDescription());
            action.type(toType(field.getType()));
            for (GraphQLArgument argument : field.getArguments()) {
                action.add(parseArgument(argument));
            }
            list.add(action);
        }
        return list;
    }

    private List<Action> buildSubAction(Action base, GraphQLSchema schema) {
        ObjectType objectType = types.get(base.getType().getName());
        if (!objectType.getIdProperty().isPresent()) {
            return Collections.emptyList();
        }

        List<Action> list = new ArrayList<>();
        for (Property property : objectType.getProperties()) {
            ObjectType propType = types.get(property.getType().getName());
            if (propType != null && propType.getIdProperty().isPresent()) {
                GraphQLType type = schema.getType(propType.getName());
                Action action = new Action().name(base.getName() + "/" + property.getName());
                action.description(property.getDescription());
                action.type(toType(type));
                if (property.getType().isArray()) {
                    action.getType().array();
                }
                for (Argument argument : base.getArguments()) {
                    action.add(argument);
                }
                list.add(action);
            }
        }
        return list;
    }

    private void parseTypes(GraphQLSchema schema) {
        for (GraphQLNamedType type : schema.getAllTypesAsList()) {
            if (type.getName().startsWith("__")) {
                continue;
            }
            if (schema.getQueryType().equals(type)) {
                continue;
            }
            if (schema.getMutationType().equals(type)) {
                continue;
            }
            parseNamedType(type);
        }
    }

    private void parseNamedType(GraphQLNamedType type) {
        if (type instanceof GraphQLObjectType) {
            GraphQLObjectType objectType = (GraphQLObjectType) type;
            ObjectType obj = new ObjectType().name(type.getName()).description(objectType.getDescription());
            for (GraphQLFieldDefinition field : objectType.getFieldDefinitions()) {
                obj.add(parseField(field));
            }
            types.put(obj.getName(), obj);
        } else if (type instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType objectType = (GraphQLInputObjectType) type;
            ObjectType obj = new ObjectType().name(type.getName()).description(objectType.getDescription());
            for (GraphQLInputObjectField field : objectType.getFieldDefinitions()) {
                obj.add(parseField(field));
            }
            types.put(obj.getName(), obj);
        } else if (type instanceof GraphQLEnumType) {
            GraphQLEnumType enumType = (GraphQLEnumType) type;
            ObjectType obj = new ObjectType().name(type.getName()).description(enumType.getDescription());
            for (GraphQLEnumValueDefinition value : enumType.getValues()) {
                Property property = new Property().name(value.getName()).type(Type.string());
                obj.add(property);
            }
            types.put(obj.getName(), obj);
        }
    }

    private Property parseField(GraphQLFieldDefinition field) {
        return parseField(field.getName(), field.getDescription(), field.getType());
    }

    private Property parseField(GraphQLInputObjectField field) {
        return parseField(field.getName(), field.getDescription(), field.getType());
    }

    private Property parseField(String name, String description, GraphQLType type) {
        Property property = new Property().name(name).description(description);

        property.type(toType(type));
        return property;
    }

    private Argument parseArgument(GraphQLArgument argument) {
        return parseArgument(argument.getName(), argument.getType());
    }

    private Argument parseArgument(String name, GraphQLType type) {
        Argument argument = new Argument().name(name);

        argument.type(toType(type));
        return argument;
    }

    private Type toType(GraphQLType t) {

        boolean isArray = false;
        if (t instanceof GraphQLNonNull) {
            t = ((GraphQLNonNull) t).getWrappedType();
        }
        if (t instanceof GraphQLList) {
            t = ((GraphQLList) t).getWrappedType();
            isArray = true;
        }
        if (t instanceof GraphQLNonNull) {
            t = ((GraphQLNonNull) t).getWrappedType();
        }

        Type type = null;
        if (t instanceof GraphQLScalarType) {
            String typeName = ((GraphQLScalarType) t).getName();
            if ("ID".equals(typeName)) {
                type = Type.id();
            } else if ("String".equals(typeName)) {
                type = Type.string();
            } else if ("Int".equals(typeName)) {
                type = Type.integer();
            } else if ("Float".equals(typeName)) {
                type = Type.floats();
            } else if ("Boolean".equals(typeName)) {
                type = Type.bool();
            } else {
                type = Type.string();
            }
        } else {
            type = Type.object(((GraphQLNamedSchemaElement) t).getName());
        }
        if (isArray) {
            type.array();
        }
        return type;
    }
}
