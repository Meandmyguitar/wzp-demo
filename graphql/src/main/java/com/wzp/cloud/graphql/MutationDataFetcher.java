package com.wzp.cloud.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanmaoly.util.lang.ExceptionUtils;
import graphql.execution.UnknownOperationException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MutationDataFetcher implements DataFetcher {

    private final Object mutation;

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer;

    public MutationDataFetcher(Object mutation) {
        this.mutation = mutation;
        parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        String name = environment.getField().getName();
        Map<String, Object> arguments = environment.getArguments();

        for (Method method : mutation.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name)) {

                String[] names = parameterNameDiscoverer.getParameterNames(method);
                if (names == null) {
                    throw new IllegalStateException("无法获取参数名称: " + method);
                }
                Parameter[] parameters = method.getParameters();
                List<Object> args = new ArrayList<>();
                for (int i = 0; i < method.getParameterCount(); i++) {
                    Parameter parameter = parameters[i];
                    String parameterName = names[i];
                    if (!arguments.containsKey(parameterName)) {
                        args.add(null);
                        continue;
                    }
                    Object v = arguments.get(parameterName);
                    ObjectMapper mapper = new ObjectMapper();
                    args.add(mapper.convertValue(v, parameter.getType()));
                }
                try {
                    return method.invoke(mutation, args.toArray());
                } catch (InvocationTargetException e) {
                    throw ExceptionUtils.throwUnchecked(e.getTargetException());
                }
            }
        }

        throw new UnknownOperationException("找不到: " + name);
    }
}
