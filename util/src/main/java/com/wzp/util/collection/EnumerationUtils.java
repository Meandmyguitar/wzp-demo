package com.wzp.util.collection;

import java.util.Enumeration;

public class EnumerationUtils extends org.apache.commons.collections.EnumerationUtils {
	
	public static <T> Enumeration<T> arrayEnumeration(T[] array) {
		return new ArrayEnumeration<T>(array);
	}

	private static class ArrayEnumeration<T> implements Enumeration<T> {
		
		private T[] array;
		
		private int index;

		public ArrayEnumeration(T[] array) {
			this.array = array;
		}

		@Override
		public boolean hasMoreElements() {
			return index < array.length;
		}

		@Override
		public T nextElement() {
			return array[index++];
		}
		
	}
}
