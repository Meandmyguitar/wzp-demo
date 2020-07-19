package com.wzp.cloud.graphql.oapi;


import graphql.language.*;
import graphql.language.Argument;
import graphql.schema.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 从schema自动生成graphql
 */
public class GraphQLAutoGenerator {

    private final GraphQLSchema schema;

    private final List<VariableDefinition> variables = new ArrayList<>();

    private final int maxDepth;

    private boolean hasSelection = false;

    public GraphQLAutoGenerator(GraphQLSchema schema) {
        this(schema, Integer.MAX_VALUE);
    }

    public GraphQLAutoGenerator(GraphQLSchema schema, int maxDepth) {
        this.schema = schema;
        this.maxDepth = maxDepth;
    }

    public Document mutation(String name) {
        if (schema.getMutationType() == null) {
            throw new IllegalStateException("schema中未定义mutation");
        }
        GraphQLFieldDefinition field = schema.getMutationType().getFieldDefinition(name);
        if (field == null) {
            throw new IllegalStateException("mutation中未定义" + name);
        }
        return Document.newDocument().definition(operation(field, OperationDefinition.Operation.MUTATION)).build();
    }

    public Document query(String name) {
        if (schema.getQueryType() == null) {
            throw new IllegalStateException("schema中未定义query");
        }
        GraphQLFieldDefinition field = schema.getQueryType().getFieldDefinition(name);
        if (field == null) {
            throw new IllegalStateException("query中未定义" + name);
        }
        return Document.newDocument().definition(operation(field, OperationDefinition.Operation.QUERY)).build();
    }

    public OperationDefinition operation(GraphQLFieldDefinition field, OperationDefinition.Operation operation) {
        Selection<?> selection = selection(field, 0);
        return OperationDefinition.newOperationDefinition().name(field.getName())
                .selectionSet(SelectionSet.newSelectionSet().selection(selection).build())
                .operation(operation)
                .variableDefinitions(variables)
                .build();
    }

    public Selection<?> selection(GraphQLFieldDefinition field, int depth) {

        Field.Builder builder = Field.newField();
        ArrayList<graphql.language.Argument> arguments = new ArrayList<>();
        for (GraphQLArgument argument : field.getArguments()) {
            String name = argument.getName();
            VariableDefinition v = VariableDefinition.newVariableDefinition(name, of(argument.getType())).build();
            variables.add(v);

            VariableReference reference = VariableReference.newVariableReference().name(name).build();
            arguments.add(Argument.newArgument(name, reference).build());
        }
        builder.arguments(arguments);

        GraphQLType unwrappedType = GraphQLUtils.unwrap(field.getType());
        if (unwrappedType instanceof GraphQLScalarType) {
            builder.name(field.getName());
        } else if (unwrappedType instanceof GraphQLEnumType) {
            builder.name(field.getName());
        } else if (unwrappedType instanceof GraphQLFieldsContainer) {
            builder.name(field.getName());
            boolean next = depth < maxDepth;
            if (!hasSelection) {
                next = true;
            }
            if (next) {
                GraphQLFieldsContainer container = (GraphQLFieldsContainer) unwrappedType;
                SelectionSet.Builder b = SelectionSet.newSelectionSet();
                for (GraphQLFieldDefinition fieldDefinition : container.getFieldDefinitions()) {
                    Selection<?> selection = selection(fieldDefinition, depth + 1);
                    if (selection != null) {
                        b.selection(selection);
                        hasSelection = true;
                    }
                }
                builder.selectionSet(b.build());
            } else {
                return null;
            }
        }
        return builder.build();
    }

    private Type<?> of(GraphQLType type) {
        if (type instanceof GraphQLNonNull) {
            return NonNullType.newNonNullType(of(((GraphQLNonNull) type).getWrappedType())).build();
        } else if (type instanceof GraphQLList) {
            return ListType.newListType(of(((GraphQLList) type).getWrappedType())).build();
        } else if (type instanceof GraphQLNamedSchemaElement) {
            GraphQLNamedSchemaElement element = (GraphQLNamedSchemaElement) type;
            return TypeName.newTypeName(element.getName()).build();
        } else {
            throw new IllegalStateException();
        }
    }
}
