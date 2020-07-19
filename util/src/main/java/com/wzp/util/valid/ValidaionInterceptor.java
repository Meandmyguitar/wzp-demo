package com.wzp.util.valid;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * bean验证拦截器
 */
public class ValidaionInterceptor implements MethodInterceptor {
	
	private Validator validator;
	
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validator = validatorFactory.getValidator();
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		for (Object item : invocation.getArguments()) {
			ValidationUtils.validate(validator, item);
		}

		return invocation.proceed();
	}

}
