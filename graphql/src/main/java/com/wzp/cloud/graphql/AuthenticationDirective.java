package com.wzp.cloud.graphql;

import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

import java.util.List;

public class AuthenticationDirective implements SchemaDirectiveWiring {

    public static final String STRATEGY_ERROR = "error";
    public static final String STRATEGY_HIDDEN = "hidden";

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {

        GraphQLDirective directive = environment.getDirective();
        List<String> values = (List<String>) directive.getArgument("name").getValue();
        String strategy = (String) directive.getArgument("strategy").getValue();

        GraphQLFieldDefinition field = environment.getElement();
        GraphQLFieldsContainer parentType = environment.getFieldsContainer();

        DataFetcher<?> fetcher = environment.getCodeRegistry().getDataFetcher(parentType, field);
        DataFetcher<?> authDataFetcher = dataFetchingEnvironment -> {

            GraphQLContext context = dataFetchingEnvironment.getContext();
            Authentication authentication = context.get("authorization");
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new NotLoginException("未登录");
            }
            if (!checkSecurity(authentication, values)) {
                if (STRATEGY_ERROR.equals(strategy)) {
                    throw new ForbiddenException("无权限");
                } else if (STRATEGY_HIDDEN.equals(strategy)) {
                    return null;
                } else {
                    throw new IllegalStateException("未知类型: " + strategy);
                }
            }

            return fetcher.get(dataFetchingEnvironment);
        };
        environment.getCodeRegistry().dataFetcher(parentType, field, authDataFetcher);

        return field;
    }

    private boolean checkSecurity(Authentication authentication, List<String> values) {
        if (values == null || values.isEmpty()) {
            return true;
        }
        for (String value : values) {
            if (authentication.getAuthorities().contains(value)) {
                return true;
            }
        }
        return false;
    }
}
