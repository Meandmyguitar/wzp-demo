package com.wzp.util.etc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;

public final class SimpleUtils {

	public static <T> T proxy(InvocationHandler h, Class<?>... classes) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = SimpleUtils.class.getClassLoader();
		}

		return proxy(loader, h, classes);
	}

	@SuppressWarnings("unchecked")
	public static <T> T proxy(ClassLoader classLoader, InvocationHandler h, Class<?>... classes) {
		return (T) Proxy.newProxyInstance(classLoader, classes, h);
	}

	/**
	 * 欺骗编译器，抛出checked异常 用法:
	 * 
	 * <pre>
	 *   try {
	 *     legacyMethodWithCheckedExceptions
	 *   } catch (Exception e) {
	 *     throw SimpleUtils.throwUnchecked(e);
	 *   }
	 * </pre>
	 * 
	 * @return
	 */
	public static RuntimeException throwUnchecked(final Throwable e) {
		SimpleUtils.<RuntimeException> throwsUnchecked(e);
		throw new AssertionError("This code should be unreachable. Something went terrible wrong here!");
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwsUnchecked(Throwable e) throws T {
		throw (T) e;
	}

	/**
	 * 快速生成uuid的字符串,长度32
	 * 
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 数字序列转字符串补0
	 * 
	 * @param seq
	 * @param length
	 * @return
	 */
	public static String seq(long seq, int length) {
		String s = String.valueOf(seq);
		StringBuilder sb = new StringBuilder();

		for (int i = s.length(); i < length; i++) {
			sb.append("0");
		}
		sb.append(s);
		return sb.toString();
	}

}
