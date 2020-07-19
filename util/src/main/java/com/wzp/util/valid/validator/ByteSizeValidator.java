package com.wzp.util.valid.validator;


import com.wzp.util.valid.ByteSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.charset.Charset;

public class ByteSizeValidator implements ConstraintValidator<ByteSize, String> {
	
	private long min;
	
	private long max;
	
	private Charset charset;

	@Override
	public void initialize(ByteSize constraintAnnotation) {
		min = constraintAnnotation.min();
		max = constraintAnnotation.max();
		charset = Charset.forName(constraintAnnotation.charset());
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		
		long length = value.getBytes(charset).length;
		
		if (length < min || length > max) {
			return false;
		}
		return true;
	}

}
