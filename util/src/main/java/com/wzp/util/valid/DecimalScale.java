package com.wzp.util.valid;


import com.wzp.util.valid.validator.DecimalScaleValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 检查Decimal的精度
 */
@Documented
@Constraint(validatedBy = DecimalScaleValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface DecimalScale {

	int min() default 0;
	
	int max();

	String message() default "精度不正确";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
