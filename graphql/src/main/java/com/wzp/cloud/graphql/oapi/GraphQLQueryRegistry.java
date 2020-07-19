package com.wzp.cloud.graphql.oapi;

import com.lanmaoly.util.lang.StreamUtils;
import graphql.language.*;
import graphql.language.Type;
import graphql.parser.Parser;
import graphql.schema.*;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GraphQLQueryRegistry {

    private final GraphQLSchema schema;

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public GraphQLQueryRegistry(GraphQLSchema schema) {
        this.schema = schema;
    }

    public void add(String query) {
        add(new Parser().parseDocument(query));
    }

    public void add(Document document) {
        List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
        for (OperationDefinition operation : operations) {
            add(operation.getName(), document, operation.getName());
        }
    }

    public void add(String name, String query, String operation) {
        Document document = new Parser().parseDocument(query);
        add(name, document, operation);
    }

    public void add(String name, Document document) {
        List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
        if (operations.size() != 1) {
            throw new IllegalArgumentException();
        }
        add(name, document, operations.get(0).getName());
    }

    public void add(String name, Document document, String operation) {
        if (this.entries.containsKey(name)) {
            throw new IllegalStateException("name: " + name + "已存在，不可重复添加");
        }

        Validator validator = new Validator();
        List<ValidationError> errors = validator.validateDocument(schema, document);
        if (errors.size() > 0) {
            throw new IllegalArgumentException(errors.get(0).getMessage());
        }
        List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
        Optional<OperationDefinition> find = operations.stream().filter(s -> s.getName().equals(operation)).findAny();
        if (!find.isPresent()) {
            throw new IllegalArgumentException("query中没有名为" + operation + "的operation");
        }
        this.entries.put(name, new Entry(AstPrinter.printAst(document), operation, document));
    }

    public Entry get(String name) {
        return entries.get(name);
    }

    public OpenAPI openApi() {
        OpenAPI openAPI = new OpenAPI().components(new Components());
        // 只处理input type
        for (GraphQLNamedType namedType : this.schema.getAllTypesAsList()) {
            if (namedType instanceof GraphQLInputObjectType) {
                openAPI.getComponents().addSchemas(namedType.getName(), schemeOfType(namedType));
            }
        }

        SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
        openAPI.getComponents().addSecuritySchemes("bearerAuth", securityScheme);
        openAPI.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

        for (String s : entries.keySet()) {
            path(openAPI, s);
        }

        return openAPI;
    }

    private void path(OpenAPI openAPI, String name) {
        Entry entry = entries.get(name);
        Document document = entry.document;

        List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
        if (operations.size() == 0) {
            throw new IllegalStateException("找不到: " + name);
        }
        for (int i = 0; i < operations.size(); i++) {
            OperationDefinition operation = operations.get(i);
            if (operation.getName().equals(entry.operation)) {
                List<Comment> comments = i == 0 ? document.getComments() : operation.getComments();
                PathItem pathItem = path(operation, StringUtils.join(StreamUtils.map(comments, Comment::getContent), "\n"));
                openAPI.path(name, pathItem);
                break;
            }
        }
    }

    private PathItem path(OperationDefinition definition, String description) {

        RequestBody requestBody = requestBody(definition);
        ApiResponse response = response(definition);

        Operation operation = new Operation();
        operation.addTagsItem("自定义查询");
        operation.summary(description);
        operation.responses(new ApiResponses().addApiResponse("200", response));
        operation.requestBody(requestBody);

        PathItem pathItem = new PathItem();
//        pathItem.description(description);
//        pathItem.summary(description);
        pathItem.post(operation);
        return pathItem;
    }

    private RequestBody requestBody(OperationDefinition definition) {
        ExtendSchema schema = variable(definition.getVariableDefinitions());
        RequestBody requestBody = new RequestBody();
        requestBody.content(
                new Content().addMediaType("application/json",
                        new MediaType().schema(schema)));
        return requestBody;
    }

    private ApiResponse response(OperationDefinition definition) {
        GraphQLObjectType type;
        if (definition.getOperation() == OperationDefinition.Operation.QUERY) {
            type = schema.getQueryType();
        } else if (definition.getOperation() == OperationDefinition.Operation.MUTATION) {
            type = schema.getMutationType();
        } else {
            throw new IllegalArgumentException("不支持的Operation: " + definition.getOperation());
        }
        ExtendSchema dataSchema = new ExtendSchema();
        for (Selection<?> selection : definition.getSelectionSet().getSelections()) {
            Field field = (Field) selection;
            dataSchema.addProperties(field.getName(), schemaOfField(type, field));
        }
        ApiResponse response = new ApiResponse();
        ExtendSchema schema = new ExtendSchema();
        schema.addProperties("errorCode", new ExtendSchema().type("string"));
        schema.addProperties("message", new ExtendSchema().type("string"));
        schema.addProperties("data", dataSchema);
        response.content(new Content().addMediaType("application/json", new MediaType().schema(schema)));
        return response;
    }

    private ExtendSchema variable(List<VariableDefinition> definition) {
        ExtendSchema schema = new ExtendSchema();
        for (VariableDefinition variable : definition) {
            ExtendSchema s = schemeOfType(getType(variable.getType()));
            // 如果properties不为空，必然是input type，可以直接引用定义
            if (s.getProperties() != null) {
                schema.addProperties(variable.getName(), new ExtendSchema().$ref(s.getName()));
            } else {
                schema.addProperties(variable.getName(), s);
            }
        }
        return schema;
    }

    private ExtendSchema schemaOfField(GraphQLObjectType type, Field field) {
        GraphQLFieldDefinition fieldDefinition = type.getFieldDefinition(field.getName());

        if (fieldDefinition.getType() instanceof GraphQLObjectType) {
            ExtendSchema schema = new ExtendSchema();
            schema.description(fieldDefinition.getDescription());
            for (Selection<?> selection : field.getSelectionSet().getSelections()) {
                ExtendSchema prop = schemaOfField((GraphQLObjectType) fieldDefinition.getType(), (Field) selection);
                schema.addProperties(((Field) selection).getName(), prop);
            }
            return schema;
        } else {
            return schemeOfType(fieldDefinition.getType());
        }
    }

    private ExtendSchema schemeOfType(GraphQLType type) {
        ExtendSchema schema = new ExtendSchema();
        if (type instanceof GraphQLNonNull) {
            type = ((GraphQLNonNull) type).getWrappedType();
        }
        if (type instanceof GraphQLList) {
            schema.type("array");
            schema.items(schemeOfType(((GraphQLList) type).getWrappedType()));
        } else if (type instanceof GraphQLNamedType) {
            GraphQLNamedType namedType = (GraphQLNamedType) type;
            String name = namedType.getName();
            if ("ID".equals(name)) {
                schema.type("string");
            } else if ("String".equals(name)) {
                schema.type("string");
            } else if ("Int".equals(name)) {
                schema.type("integer").format("int64");
            } else if ("Float".equals(name)) {
                schema.type("number").format("double");
            } else if ("Boolean".equals(name)) {
                schema.type("number");
            } else if ("DateTime".equals(name)) {
                schema.type("string").format("date-time");
            } else if ("LocalDateTime".equals(name)) {
                schema.type("string").format("date-time");
            } else {
                schema = schemaOfComplexType(namedType);
            }
        } else {
            throw new IllegalStateException();
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    private ExtendSchema schemaOfComplexType(GraphQLNamedType type) {
        ExtendSchema schema = new ExtendSchema();
        if (type instanceof GraphQLEnumType) {
            GraphQLEnumType enumType = (GraphQLEnumType) type;
            ArrayList<String> values = new ArrayList<>();
            for (GraphQLEnumValueDefinition value : enumType.getValues()) {
                values.add(value.getName());
            }
            schema.type("string");
            schema.setEnum(values);
        } else if (type instanceof GraphQLObjectType) {
            GraphQLObjectType objectType = (GraphQLObjectType) type;
            schema.name(objectType.getName()).description(objectType.getDescription());
            for (GraphQLFieldDefinition field : objectType.getFieldDefinitions()) {
                schema.addProperties(field.getName(), schemeOfType(field.getType()).description(field.getDescription()));
            }
        } else if (type instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType objectType = (GraphQLInputObjectType) type;
            schema.name(objectType.getName()).description(objectType.getDescription());
            for (GraphQLInputObjectField field : objectType.getFieldDefinitions()) {
                schema.addProperties(field.getName(), schemeOfType(field.getType()).description(field.getDescription()));
            }
        } else {
            throw new IllegalStateException();
        }
        return schema;
    }

    private GraphQLType getType(Type<?> type) {
        if (type instanceof NonNullType) {
            return GraphQLNonNull.nonNull(getType(((NonNullType) type).getType()));
        } else if (type instanceof ListType) {
            return GraphQLList.list(getType(((ListType) type).getType()));
        } else if (type instanceof TypeName) {
            return schema.getType(((TypeName) type).getName());
        } else {
            throw new IllegalStateException();
        }
    }

    public static class Entry {

        private final String query;

        private final String operation;

        private final Document document;

        public Entry(String query, String operation, Document document) {
            this.query = query;
            this.operation = operation;
            this.document = document;
        }

        public String getQuery() {
            return query;
        }

        public Document getDocument() {
            return document;
        }

        public String getOperation() {
            return operation;
        }
    }

}




















