package com.wzp.cloud.support.tracing;

public interface TraceableAction<T> {

    T run() throws Throwable;
}
