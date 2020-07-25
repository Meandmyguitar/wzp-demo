package com.wzp.util.dynamic;


import com.google.gson.*;
import com.wzp.util.etc.SimpleUtils;
import com.wzp.util.time.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class SmartInvoker {

    private GsonBuilder gsonBuilder = new GsonBuilder();

    public SmartInvoker() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        registerTypeAdapter(Date.class, new DateDeserializer());
    }

    public <T> void registerTypeAdapter(Type type, Deserializer<T> deserializer) {
        gsonBuilder.registerTypeAdapter(type, new JsonDeserializerAdapter<T>(deserializer));
    }

    public Object invoke(Object obj, String name, Class<?> targetClass, Object... params) {

        Class<?> clazz = targetClass;
        if (clazz == null) {
            clazz = obj.getClass();
        }

        List<Method> methods = filter(clazz.getMethods(), name, params);
        if (methods.size() == 0) {
            throw new IllegalArgumentException("method不存在:" + name);
        } else if (methods.size() > 1) {
            throw new IllegalArgumentException("找到多条method:" + name);
        }

        Method method = null;

        if (method == null) {
            params = convertParams(methods.get(0), params).toArray();
        }
        try {
            return methods.get(0).invoke(obj, params);
        } catch (InvocationTargetException e) {
            throw SimpleUtils.throwUnchecked(e.getCause());
        } catch (IllegalAccessException e) {
            throw SimpleUtils.throwUnchecked(e.getCause());
        }
    }

    private boolean isCompletelyMatch(Method method, String name, Object[] params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                continue;
            }
            if (!method.getParameterTypes()[i].isAssignableFrom(params[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private List<Method> filter(Method[] methods, String name, Object[] params) {
        List<Method> list = filterByParamCount(methods, name, params);
        for (Method m : list) {
            if (isCompletelyMatch(m, name, params)) {
                return Arrays.asList(m);
            }
        }
        return list;
    }

    // 过滤掉参数个数不一样的
    private List<Method> filterByParamCount(Method[] methods, String name, Object[] params) {
        ArrayList<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getParameterTypes().length != params.length) {
                continue;
            }
            if (!method.getName().equals(name)) {
                continue;
            }
            list.add(method);
        }
        return list;
    }

    private List<Object> convertParams(Method method, Object[] params) {

        Class<?>[] classes = method.getParameterTypes();
        Type[] types = method.getGenericParameterTypes();
        ArrayList<Object> convertedParams = new ArrayList<Object>();

        for (int i = 0; i < classes.length; i++) {

            Object value = null;
            if (params[i] == null) {
                value = null;
            } else if (canAssign(classes[i])) {
                if (classes[i].isAssignableFrom(params[i].getClass())) {
                    value = params[i];
                } else {
                    throw new IllegalArgumentException("无法转换接口或者抽象类的参数");
                }
            } else {
                value = convert(params[i], types[i]);
            }

            convertedParams.add(value);
        }

        return convertedParams;
    }

    private boolean canAssign(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return false;
        }
        if (List.class.equals(clazz)
                || Map.class.equals(clazz)) {
            return false;
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return true;
        }
        return false;
    }

    private Object convert(Object source, Type target) {
        Gson g = gsonBuilder.create();
        String json = g.toJson(source);
        return g.fromJson(json, target);
    }

    class JsonDeserializerAdapter<T> implements JsonDeserializer<T> {

        private Deserializer<T> deserializer;

        public JsonDeserializerAdapter(Deserializer<T> deserializer) {
            this.deserializer = deserializer;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT,
                             JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    return deserializer.deserialize(primitive.getAsNumber());
                } else if (primitive.isString()) {
                    return deserializer.deserialize(primitive.getAsString());
                } else if (primitive.isBoolean()) {
                    return deserializer.deserialize(primitive.getAsBoolean());
                }
            }
//            ForkJoinPool
//            Executors
            return deserializer.deserialize(json.getAsString());
        }

    }

    class DateDeserializer implements Deserializer<Date> {

        public Date deserialize(Object source) {
            if (source instanceof String) {
                try {
                    return DateUtils.parseDate((String) source, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
                } catch (ParseException e) {
                    throw new IllegalArgumentException("无法转换为Date: " + source);
                }
            } else if (source instanceof Number) {
                return new Date(((Number) source).longValue());
            } else if (source instanceof Date) {
                return (Date) source;
            } else {
                throw new IllegalArgumentException("无法转换为Date: " + source);
            }
        }

    }
}
