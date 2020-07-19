package com.wzp.cloud.graphql;

@SuppressWarnings("WeakerAccess")
public class ExceptionUtils {

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
     */
    public static RuntimeException throwUnchecked(final Throwable e) {
        ExceptionUtils.throwsUnchecked(e);
        throw new AssertionError("This code should be unreachable. Something went terrible wrong here!");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwsUnchecked(Throwable e) throws T {
        throw (T) e;
    }
}
