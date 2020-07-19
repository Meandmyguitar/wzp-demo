package com.wzp.util.interceptor;

public interface Interceptor<T> {

	Object invoke(Invocation<T> invocation) throws Throwable;
}
