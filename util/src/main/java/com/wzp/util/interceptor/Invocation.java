package com.wzp.util.interceptor;

public interface Invocation<T> {
	
	Object proceed() throws Throwable;
	
	T getContext();
}
