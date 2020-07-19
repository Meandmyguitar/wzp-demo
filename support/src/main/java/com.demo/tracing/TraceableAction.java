package com.demo.tracing;

public interface TraceableAction<T> {

    T run() throws Throwable;
}
