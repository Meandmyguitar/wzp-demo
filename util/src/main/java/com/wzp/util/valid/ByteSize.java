package com.wzp.util.valid;


import com.wzp.util.valid.validator.ByteSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 按照字节限制字符串的大小
 */
@Documented
@Constraint(validatedBy = ByteSizeValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface ByteSize {
	
	String charset() default "utf-8";
	
	long min() default 0;

	long max() default Long.MAX_VALUE;

	String message() default "{org.hibernate.validator.constraints.Range.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}