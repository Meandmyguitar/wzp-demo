package com.wzp.util.collection;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {

    /**
     * 将对象转换为Iterator,如果不支持会返回null
     * <p>支持如下类型
     * <ul>
     *   <li>null被转换为空的Iterator</li>
     *   <li>Iterable</li>
     *   <li>Enumeration</li>
     *   <li>Map</li>
     *   <li>Array</li>
     * </ul>
     * </p>
     *
     * @param object
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Iterator<?> asIterator(Object object) {
        if (object instanceof Iterator) {
            return ((Iterator) object);
        } else {
            return asIterable(object).iterator();
        }
    }

    /**
     * 将对象转换为Iterable,如果不支持会返回null
     * <p>支持如下类型
     * <ul>
     *   <li>null被转换为空的Iterable</li>
     *   <li>Enumeration</li>
     *   <li>Map</li>
     *   <li>Array</li>
     * </ul>
     * </p>
     *
     * @param object
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Iterable asIterable(Object object) {
        if (object == null) {
            return Collections.emptyList();
        } else if (object instanceof Iterable) {
            return (Iterable) object;
        } else if (object instanceof Map) {
            return ((Map) object).entrySet();
        } else if (object.getClass().isArray()) {
            return new ArrayIterable(object);
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class ArrayIterable implements Iterable {

        private Object array;

        public ArrayIterable(Object array) {
            this.array = array;
        }

        @Override
        public Iterator iterator() {
            return IteratorUtils.arrayIterator(array);
        }

    }
}
