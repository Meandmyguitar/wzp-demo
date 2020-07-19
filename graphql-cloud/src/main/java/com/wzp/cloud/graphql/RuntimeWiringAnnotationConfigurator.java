package com.wzp.cloud.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanmaoly.cloud.graphql.query.QueryOption;
import com.lanmaoly.cloud.graphql.type.QueryOptionInput;
import com.lanmaoly.util.lang.ExceptionUtils;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import org.dataloader.DataLoader;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.io.IOException;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeWiringAnnotationConfigurator {

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Map<Class<?>, Object> types = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    private Object context;

    public RuntimeWiring.Builder configuration(RuntimeWiring.Builder builder, TypeDefinitionRegistry typeRegistry) {
        types.entrySet().stream()
                .map(e -> wiringType(e.getValue(), e.getKey(), typeRegistry))
                .forEach(builder::type);
        return builder;
    }

    public RuntimeWiringAnnotationConfigurator root(Object root) {
        types.put(root.getClass(), root);
        return this;
    }

    public RuntimeWiringAnnotationConfigurator type(Class<?> type) {
        types.put(type, null);
        return this;
    }

    public RuntimeWiringAnnotationConfigurator context(Object context) {
        this.context = context;
        return this;
    }

    private TypeRuntimeWiring.Builder wiringType(Object object, Type type, TypeDefinitionRegistry typeRegistry) {

        Class<?> clazz = null;
        if (type instanceof Class) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            throw new IllegalStateException("未识别的Type: " + type);
        }

        TypeRuntimeWiring.Builder typeBuilder = typeBuilder(type, typeRegistry);
        for (Method method : clazz.getMethods()) {
            GraphQLField field = method.getAnnotation(GraphQLField.class);
            if (field != null) {
                wiringField(object, typeBuilder, method);
            }
        }
        return typeBuilder;
    }

    private void wiringField(Object source, TypeRuntimeWiring.Builder builder, Method method) {
        builder.dataFetcher(method.getName(), new MethodDataFetcher<>(source, method));
    }

    private TypeRuntimeWiring.Builder typeBuilder(Type type, TypeDefinitionRegistry typeRegistry) {
        String typeName = typeName(type);
        if (!typeRegistry.getType(typeName).isPresent()) {
            throw new IllegalStateException("未定义的Type: " + typeName);
        }
        return new TypeRuntimeWiring.Builder().typeName(typeName);
    }

    private String typeName(Type type) {

        Class clazz = null;
        ArrayList<String> list = new ArrayList<>();

        if (type instanceof Class) {
            clazz = (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            clazz = (Class) parameterizedType.getRawType();
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();

            list.add(clazz.getSimpleName());
            for (Type type1 : actualTypeArguments) {
                list.add(typeName(type1));
            }
        }

        GraphQLType t = (GraphQLType) clazz.getAnnotation(GraphQLType.class);
        if (t != null && t.name().length() > 0) {
            return MessageFormat.format(t.name(), list.toArray());
        } else {
            return clazz.getSimpleName();
        }
    }

    class MethodDataFetcher<T> implements DataFetcher<T> {

        private final Object object;

        private final Method method;

        public MethodDataFetcher(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(DataFetchingEnvironment environment) throws Exception {

            checkSecurity(environment);

            Map<String, Object> arguments = environment.getArguments();
            Object source = object == null ? environment.getSource() : object;

            String[] names = nameDiscoverer.getParameterNames(method);
            if (names == null) {
                throw new IllegalStateException("无法获取参数名称: " + method);
            }

            // 参数转换
            Parameter[] parameters = method.getParameters();
            List<Object> args = new ArrayList<>();
            for (int i = 0; i < method.getParameterCount(); i++) {
                args.add(convertToArgumentValue(parameters[i], names[i], environment));
            }
            try {
                return (T) method.invoke(source, args.toArray());
            } catch (InvocationTargetException e) {
                throw ExceptionUtils.throwUnchecked(e.getTargetException());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("调用" + method + "时出错", e);
            }
        }

        private void checkSecurity(DataFetchingEnvironment environment) {
            Secure secured = method.getAnnotation(Secure.class);
            if (secured != null) {
                GraphQLContext context = environment.getContext();
                Context ctx = context.get("context");
                Authentication authentication = ctx.getAuthentication();
                if (authentication.getPrincipal() == null) {
                    throw new NotLoginException("用户未登录，无权访问: " + environment.getExecutionStepInfo().getPath());
                }
                if (secured.value().length == 0) {
                    return;
                }
                for (String s : secured.value()) {
                    if (authentication.getAuthorities().contains(s)) {
                        return;
                    }
                }
                throw new ForbiddenException("没有访问权限: " + environment.getExecutionStepInfo().getPath());
            }
        }

        private Object convertToArgumentValue(Parameter parameter, String parameterName, DataFetchingEnvironment environment) {
            if (parameter.getType().equals(DataLoader.class)) {
                return environment.getDataLoader(parameterName);
            } else if (parameter.getType().equals(DataFetchingEnvironment.class)) {
                return environment;
            } else if (parameter.getType().equals(Context.class)) {
                GraphQLContext context = environment.getContext();
                return context.get("context");
            } else if (parameter.getType().equals(QueryOption.class)) {
                QueryOptionInput input = mapper.convertValue(environment.getArguments().get(parameterName), QueryOptionInput.class);
                QueryOption.Builder builder = new QueryOption.Builder();
                if (input != null) {
                    builder.offset(input.getOffset()).limit(input.getLimit());
                    input.getSorts().forEach(s -> {
                        builder.sort(s.getField(), s.getDirection());
                    });
                }
                GraphQLOutputType dataSetType = environment.getFieldDefinition().getType();
                if (dataSetType instanceof GraphQLObjectType) {
                    String name = ((GraphQLObjectType) dataSetType).getName();
                    if (name.endsWith("DataSet")) {
                        boolean total = environment.getSelectionSet().getField("total") != null;
                        builder.includeTotal(total);
                    }
                }
                return builder.build();
            } else if (context != null && parameter.getType().isAssignableFrom(context.getClass())) {
                return context;
            } else if (environment.getArguments().containsKey(parameterName)) {
                Object v = environment.getArgument(parameterName);
                return mapper.convertValue(v, mapper.constructType(parameter.getParameterizedType()));
            }

            return null;
        }
    }


    public static void main(String[] args) throws IOException {


    }
}
