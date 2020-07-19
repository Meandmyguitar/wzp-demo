package com.wzp.cloud.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodDataFetcher<T> implements DataFetcher<T> {

    private final Object object;

    private final Method method;

    private DataLoaderRegistry dataLoaderRegistry;

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public MethodDataFetcher(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    public MethodDataFetcher(Object object, Method method, DataLoaderRegistry dataLoaderRegistry) {
        this.object = object;
        this.method = method;
        this.dataLoaderRegistry = dataLoaderRegistry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(DataFetchingEnvironment environment) throws Exception {

        Map<String, Object> arguments = environment.getArguments();
        Object source = object == null ? environment.getSource() : object;

        String[] names = parameterNameDiscoverer.getParameterNames(method);
        if (names == null) {
            throw new IllegalStateException("无法获取参数名称: " + method);
        }

        // 参数转换
        Parameter[] parameters = method.getParameters();
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter parameter = parameters[i];
            String parameterName = names[i];

            if (dataLoaderRegistry != null && DataLoader.class.isAssignableFrom(parameter.getType())) {
                args.add(dataLoaderRegistry.getDataLoader(parameterName));
            } else if (arguments.containsKey(parameterName)) {
                Object v = arguments.get(parameterName);
                ObjectMapper mapper = new ObjectMapper();
                args.add(mapper.convertValue(v, parameter.getType()));
            } else {
                args.add(null);
            }
        }
        try {
            return (T) method.invoke(source, args.toArray());
        } catch (InvocationTargetException e) {
            throw ExceptionUtils.throwUnchecked(e.getTargetException());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("调用" + method + "时出错", e);
        }
    }
}
