package com.wzp.util.collection;

import java.util.Iterator;

public class IteratorUtils extends org.apache.commons.collections.IteratorUtils {

	public static <T> Iterable<T> asIterable(final Iterator<T> iterator) {
        return new Iterable<T>() {
        	boolean returned = false;
			@Override
			public synchronized Iterator<T> iterator() {
				if (returned) {
					throw new IllegalStateException("一次性Iterable");
				}
				returned = true;
				return iterator;
			}
		};
    }
	

}
