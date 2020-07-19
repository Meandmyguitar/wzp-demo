package com.wzp.cloud.graphql.oapi;

import com.lanmaoly.util.lang.StreamUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.List;

public class OpenAPIGenerator {

    public RestStore store;

    public OpenAPIGenerator(RestStore store) {
        this.store = store;
    }

    public OpenAPI build() {

        OpenAPI oas = new OpenAPI();
        oas.setComponents(new Components());
        for (ObjectType type : store.getTypes().values()) {
            Schema schema = toSchema(type);
            oas.getComponents().addSchemas(schema.getName(), schema);
        }

        for (Action action : store.getActions().values()) {
            addPath(oas, action);
        }

        return oas;
    }

    private void addPath(OpenAPI oas, Action action) {

        RequestBody requestBody = new RequestBody();
        requestBody.content(new Content().addMediaType("application/json", new MediaType().schema(toSchema(action.getArguments()))));

        ApiResponse response = new ApiResponse();
        Schema dataSchema = toSchema(action.getType());
        Schema schema = new Schema();
        schema.addProperties("errorCode", new Schema().type("string"));
        schema.addProperties("message", new Schema().type("string"));
        schema.addProperties("data", dataSchema);
        response.content(new Content().addMediaType("application/json", new MediaType().schema(schema)));

        Operation operation = new Operation();
        operation.description(action.getDescription());
        if (action.getName().startsWith("query")) {
            operation.addTagsItem("查询");
        } else if (action.getName().startsWith("mutation")) {
            operation.addTagsItem("变更");
        }

        operation.responses(new ApiResponses().addApiResponse("200", response));
        operation.requestBody(requestBody);

        PathItem pathItem = new PathItem();
        pathItem.description(operation.getDescription());
        pathItem.post(operation);
        oas.path("/graphql/rest/" + action.getName(), pathItem);
    }

    private ExtendSchema toSchema(List<Argument> arguments) {
        ExtendSchema schema = new ExtendSchema();
        for (Argument argument : arguments) {
            schema.addProperties(argument.getName(), toSchema(argument.getType()));
        }
        return schema;
    }

    private ExtendSchema toSchema(ObjectType objectType) {
        ExtendSchema schema = new ExtendSchema();
        schema.name(objectType.getName()).description(objectType.getDescription());
        for (Property property : objectType.getProperties()) {
            if (objectType.isEntity() && property.getType().getType() == PropertyTypes.Object) {
                // 实体的关系实体不输出
                continue;
            }
            schema.addProperties(property.getName(), toSchema(property.getType()).description(property.getDescription()));
        }
        return schema;
    }

    private ExtendSchema toSchema(Type type) {
        ExtendSchema schema = new ExtendSchema();
        if ("ID".equals(type.getName())) {
            schema.type("string");
        } else if ("String".equals(type.getName())) {
            schema.type("string");
        } else if ("Int".equals(type.getName())) {
            schema.type("integer").format("int64");
        } else if ("Float".equals(type.getName())) {
            schema.type("number").format("double");
        } else if ("Boolean".equals(type.getName())) {
            schema.type("number");
        } else if ("DateTime".equals(type.getName())) {
            schema.type("string").format("date-time");
        } else if ("LocalDateTime".equals(type.getName())) {
            schema.type("string").format("date-time");
        }

        if (type.getType() == PropertyTypes.Object) {
            ObjectType objectType = store.getTypes().get(type.getName());
            if (objectType.isEnum()) {
                List<String> list = StreamUtils.map(objectType.getProperties(), Property::getName);
                schema.setEnum(list);
                schema.type("string");
            } else {
                schema.$ref(type.getName());
            }
        }

        if (type.isArray()) {
            schema = new ExtendSchema().items(schema);
            schema.type("array");
        }

        return schema;
    }

}
