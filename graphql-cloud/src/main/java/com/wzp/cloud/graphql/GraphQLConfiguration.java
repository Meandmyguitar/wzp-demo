package com.wzp.cloud.graphql;

import com.lanmaoly.cloud.graphql.oapi.DefaultOpenAPIOfGraphQLAdapter;
import com.lanmaoly.cloud.graphql.oapi.GraphQLOpenAPIAdapter;
import com.lanmaoly.cloud.graphql.oapi.OpenAPIAdapter;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@ConditionalOnBean(DefaultGraphQLConfigurer.class)
@Configuration
public class GraphQLConfiguration {

    private final DefaultGraphQLConfigurer configurer;

    public GraphQLConfiguration(@Autowired(required = false) DefaultGraphQLConfigurer configurer) throws IOException {
        this.configurer = configurer;
        if (this.configurer != null) {
            this.configurer.init();
        }
    }

    @ConditionalOnBean(DefaultGraphQLConfigurer.class)
    @Bean
    GraphQLSchema createGraphQLSchema() throws IOException {
        if (configurer == null) {
            return null;
        }
        return configurer.getSchema();
    }

    @ConditionalOnBean(DefaultGraphQLConfigurer.class)
    @Bean
    GraphQL createGraphQL() throws IOException {
        if (configurer == null) {
            return null;
        }
        return configurer.getGraphQL();
    }

    @ConditionalOnBean(DefaultGraphQLConfigurer.class)
    @Bean
    GraphQLExecutor graphQLExecutor() {
        return new GraphQLExecutor(configurer);
    }

    @ConditionalOnBean(DefaultGraphQLConfigurer.class)
    @Bean
    OpenAPIAdapter openAPIAdapter(GraphQLSchema schema, GraphQLExecutor executor) {
        return new OpenAPIAdapter(schema, executor);
    }

    @ConditionalOnBean(DefaultGraphQLConfigurer.class)
    @Bean
    GraphQLOpenAPIAdapter graphQLOpenAPIAdapter(GraphQLSchema schema, GraphQLExecutor executor, ResourcePatternResolver patternResolver) throws IOException {
        return new DefaultOpenAPIOfGraphQLAdapter(schema, executor, patternResolver);
    }
}
