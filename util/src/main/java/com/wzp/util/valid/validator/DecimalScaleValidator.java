package com.wzp.util.valid.validator;


import com.wzp.util.valid.DecimalScale;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class DecimalScaleValidator implements ConstraintValidator<DecimalScale, BigDecimal> {
	
	private int min;
	
	private int max;

	@Override
	public void initialize(DecimalScale constraintAnnotation) {
		min = constraintAnnotation.min();
		max = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		if (value.scale() < min || value.scale() > max) {
			return false;
		}
		return true;
	}

}
