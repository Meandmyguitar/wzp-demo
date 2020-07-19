package com.wzp.util.interceptor;

import java.util.ArrayList;
import java.util.Collection;

public class InterceptorBuilder<T> {
	
	public static <T> InterceptorBuilder<T> create() {
		return new InterceptorBuilder<T>();
	}

	private ArrayList<Interceptor<T>> list = new ArrayList<Interceptor<T>>();
	
	private T context;
	
	public InterceptorBuilder<T> add(Interceptor<T> chain) {
		list.add(chain);
		return this;
	}
	
	public InterceptorBuilder<T> addAll(Collection<Interceptor<T>> chains) {
		list.addAll(chains);
		return this;
	}
	
	public InterceptorBuilder<T> context(T context) {
		this.context = context;
		return this;
	}
	
	public Invocation<T> build() {
		DefaultInvocation head = null;
		for (int i = list.size() - 1; i >= 0; i--) {
			Interceptor<T> invocation = list.get(i);
			head = new DefaultInvocation(head, invocation, context);
		}
		head = new DefaultInvocation(head, null, null);
		return head;
	}
	
	class DefaultInvocation implements Invocation<T> {

		private DefaultInvocation next;

		private Interceptor<T> interceptor;

		private T context;

		public DefaultInvocation(DefaultInvocation next, Interceptor<T> interceptor, T context) {
			this.next = next;
			this.interceptor = interceptor;
			this.context = context;
		}

		@Override
		public Object proceed() throws Throwable {
			if (next == null) {
				throw new IllegalStateException("已到达调用链尾部");
			}
			return next.interceptor.invoke(next);
		}

		@Override
		public T getContext() {
			return context;
		}
	}
}
