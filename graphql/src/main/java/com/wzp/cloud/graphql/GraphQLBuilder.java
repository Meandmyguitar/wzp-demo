package com.wzp.cloud.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanmaoly.cloud.graphql.query.Query;
import com.lanmaoly.cloud.graphql.query.QueryContext;
import com.lanmaoly.cloud.graphql.query.QueryOption;
import com.lanmaoly.cloud.graphql.type.QueryOptionInput;
import graphql.schema.DataFetcher;
import graphql.schema.idl.TypeRuntimeWiring;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.dataloader.DataLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GraphQLBuilder {

    private final Map<String, TypeBuilder> builderMap = new HashMap<>();

    private final Map<String, Supplier<DataLoader<?, ?>>> dataLoaderMap = new HashMap<>();

    private Object mutation;

    public class TypeBuilder {

        private final String name;

        private final TypeRuntimeWiring.Builder builder;

        TypeBuilder(String name, TypeRuntimeWiring.Builder builder) {
            this.name = name;
            this.builder = builder;
        }

        public TypeBuilder query(String name, Query<Map<String, Object>> query) {
            this.builder.dataFetcher(name, newDataFetcherByQuery(query));
            return this;
        }

        public TypeBuilder object(String name, Query<Set<?>> query) {
            this.builder.dataFetcher(name, newDataLoaderDataFetcher(this.name + "/" + name, query, false));
            return this;
        }

        public TypeBuilder array(String name, Query<Set<?>> query) {
            this.builder.dataFetcher(name, newDataLoaderDataFetcher(this.name + "/" + name, query, true));
            return this;
        }

        public TypeBuilder method(String name, Object object, Method method) {
            this.builder.dataFetcher(name, new MethodDataFetcher<>(object, method));
            return this;
        }
    }

    public TypeBuilder type(String name) {
        return builderMap.computeIfAbsent(name, k -> new TypeBuilder(name, TypeRuntimeWiring.newTypeWiring(name)));
    }

    public void mutation(Object mutation) {
        this.mutation = mutation;
    }

    public void build(RuntimeWiringBuilder b) {
        dataLoaderMap.forEach(b::registerDataLoader);
        builderMap.forEach((k, v) -> b.type(v.builder));

        if (mutation != null) {
            b.type(TypeRuntimeWiring.newTypeWiring("Mutation").defaultDataFetcher(newMutationDataFetcher(mutation)));
        }
    }

    private DataFetcher<?> newMutationDataFetcher(Object object) {
        return new MutationDataFetcher(object);
    }

    private DataFetcher<?> newDataFetcherByQuery(Query<Map<String, Object>> query) {

        return environment -> {
            boolean total = environment.getSelectionSet().getField("total") != null;
            Object arg = environment.getArgument("option");
            QueryOptionInput input = new ObjectMapper().convertValue(arg, QueryOptionInput.class);
            if (input == null) {
                input = new QueryOptionInput();
            }
//            if (input.getSorts() == null) {
//                input.setSorts(Collections.emptyList());
//            }

            Map<String, Object> filters = new HashMap<>(environment.getArguments());
            filters.remove("option");

            QueryOption option = new QueryOption(
                    input.getOffset(),
                    input.getLimit(),
                    input.getSorts().stream().map(o -> new QueryOption.Sort(o.getField(), o.getDirection())).collect(Collectors.toList()),
                    total);
            return query.execute(QueryContext.of(filters, option));
        };
    }

    private DataFetcher<?> newDataLoaderDataFetcher(String dataLoaderName, Query<Set<?>> query, boolean isArray) {
        dataLoaderMap.put(dataLoaderName, () -> createDataLoaderDataFetcher(query, isArray));
        return environment -> {
            DataLoader<Object, ?> loader = environment.getDataLoader(dataLoaderName);
            return loader.load(idOf(environment.getSource()));
        };
    }

    private DataLoader<?, ?> createDataLoaderDataFetcher(Query<Set<?>> query, boolean isArray) {
        JoinBatchLoader<?, ?> joinBatchLoader = new JoinBatchLoader(query, isArray);
        return DataLoader.newMappedDataLoader(joinBatchLoader);
    }


    private Object idOf(Object v) {
        Object key = readProperty(v, "id");
        if (key == null) {
            throw new IllegalStateException("获取对象的id失败,值不能为null");
        }
        return key;
    }

    private Object readProperty(Object v, String name) {
        Objects.requireNonNull(v);
        Objects.requireNonNull(name);
        try {
            return BeanUtilsBean2.getInstance().getProperty(v, name);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("获取对象的属性失败: " + name, e);
        }
    }
}
