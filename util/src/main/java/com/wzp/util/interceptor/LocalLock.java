
package com.wzp.util.interceptor;

import java.lang.annotation.*;

/**
 * 锁的注解
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LocalLock {
    /**
     * @author fly
     */
    String key() default "";
}