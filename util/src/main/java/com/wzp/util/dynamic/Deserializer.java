package com.wzp.util.dynamic;


public interface Deserializer<T> {

	public T deserialize(Object source);
}
