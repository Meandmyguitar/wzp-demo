package com.wzp.cloud.graphql;

import com.wzp.cloud.graphql.query.Query;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.io.IOUtils;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class DefaultGraphQLConfigurer implements GraphQLConfigurer, DataLoaderRegistryFactory {

    private final ResourcePatternResolver resolver;
    private final DataLoaderRegistry registry = new DataLoaderRegistry();
    private final RuntimeWiringAnnotationConfigurator configurator = new RuntimeWiringAnnotationConfigurator();
    private final ArrayList<String> schemaPaths = new ArrayList<>();
    private final Map<String, Supplier<DataLoader<?, ?>>> loaders = new HashMap<>();
    private ErrorHandler errorHandler;

    private GraphQLSchema schema;
    private GraphQL graphQL;

    public DefaultGraphQLConfigurer(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }

    DataLoaderRegistry getDataLoaderRegistry() {
        return registry;
    }

    @Override
    public DataLoaderRegistry createDataLoaderRegistry() {
        DataLoaderRegistry registry = new DataLoaderRegistry();
        loaders.forEach((k, v) -> registry.register(k, v.get()));
        return registry;
    }

    ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public final GraphQLConfigurer schema(String... paths) {
        schemaPaths.clear();
        schemaPaths.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public final GraphQLConfigurer errorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    @Override
    public final DataLoaderBuilder loader() {
        return new DataLoaderBuilderImpl();
    }

    @Override
    public final WiringBuilder wiring() {
        return new WiringBuilderImpl();
    }

    protected void configure(GraphQLConfigurer configurer) {

    }

    void init() throws IOException {
        configure(this);

        this.schema = makeSchema();
        this.graphQL = createGraphQL(schema);
    }

    private String loadSchema() throws IOException {

        Resource base = resolver.getResource("classpath:/graphql/base.graphqls");
        StringBuilder sb = new StringBuilder();
        sb.append(IOUtils.toString(base.getURI(), StandardCharsets.UTF_8)).append("\n");

        for (String schemaPath : schemaPaths) {
            Resource[] resources = resolver.getResources(schemaPath);
            for (Resource resource : resources) {
                sb.append(IOUtils.toString(resource.getURI(), StandardCharsets.UTF_8));
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private GraphQL createGraphQL(GraphQLSchema schema) {
        ChainedInstrumentation chain = makeInstrumentation();

        ExecutionStrategy strategy = new AsyncExecutionStrategy(new DefaultDataFetcherExceptionHandler());

        return GraphQL
                .newGraphQL(schema)
                .queryExecutionStrategy(strategy)
                .mutationExecutionStrategy(strategy)
                .instrumentation(chain).build();
    }

    private GraphQLSchema makeSchema() throws IOException {
        return makeSchema(loadSchema());
    }

    private GraphQLSchema makeSchema(String schemaContent) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaContent);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, makeRuntimeWiring(typeRegistry));
    }

    private RuntimeWiring makeRuntimeWiring(TypeDefinitionRegistry typeRegistry) {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        configurator.configuration(builder, typeRegistry);
        builder.scalar(ExtendedScalars.DateTime);
        builder.scalar(new LocalDateTimeScalar());
        builder.directive("fwAuthority", new AuthenticationDirective());
        return builder.build();
    }

    private ChainedInstrumentation makeInstrumentation() {
        DataLoaderDispatcherInstrumentationOptions options = DataLoaderDispatcherInstrumentationOptions
                .newOptions().includeStatistics(true);
        DataLoaderDispatcherInstrumentation dispatcherInstrumentation
                = new DataLoaderDispatcherInstrumentation(options);
        return new ChainedInstrumentation(Collections.singletonList(dispatcherInstrumentation));
    }

    public class WiringBuilderImpl implements WiringBuilder {

        @Override
        public WiringBuilder root(Object query) {
            configurator.root(query);
            return this;
        }

        @Override
        public WiringBuilder type(Class<?> clazz) {
            configurator.type(clazz);
            return this;
        }

        @Override
        public WiringBuilder context(Object context) {
            configurator.context(context);
            return this;
        }
    }


    public class DataLoaderBuilderImpl implements DataLoaderBuilder {

        @Override
        public <K, V> DataLoaderBuilder with(String name, Supplier<MappedBatchLoader<K, V>> supplier) {
            loaders.put(name, () -> DataLoader.newMappedDataLoader(supplier.get()));
            return this;
        }

        @Override
        public <K, V> DataLoaderBuilder with(String name, FlatBatchLoader<K, V> fetcher, boolean isArray) {
            loaders.put(name, () -> DataLoader.newMappedDataLoader(new AutoGroupBatchLoader<>(fetcher, isArray)));
            return this;
        }

        @Override
        public <K> DataLoaderBuilder with(String name, Query<Set<K>> query, boolean isArray) {
            with(name, () -> new JoinBatchLoader<>(query, isArray));
            return this;
        }
    }
}
