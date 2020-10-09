package com.wzp.util.valid;

import com.wzp.util.etc.StringUtils;
import com.wzp.util.pubsub.NoOpPubSub;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * 验证工具类
 */
public class ValidationUtils {
	
	private static Validator VALIDATOR = null;
	
	static {
		VALIDATOR = simpleValidatorFactory().getValidator();
	}
	
	public static ValidatorFactory simpleValidatorFactory() {
		Configuration<?> cfg = Validation.byDefaultProvider().configure();
		cfg.messageInterpolator(new ParameterMessageInterpolator());
		return cfg.buildValidatorFactory();
	}

	/**
	 * 验证一个bean是否正确
	 * @param bean
	 */
	public static void validate(Object bean) {
		validate(VALIDATOR, bean);
	}

	public static void validate(Validator validator, Object bean) {
		if (validator == null) {
			validator = VALIDATOR;
		}
		Set<ConstraintViolation<Object>> result = validator.<Object>validate(bean);
		if (result.size() > 0) {
			ConstraintViolation<Object> v = result.iterator().next();
			String message = v.getPropertyPath() + " " + v.getMessage(); 
			throw new ConstraintViolationException(message, result);
		}
	}
	
	/**
	 * 构造报错信息
	 * @param v
	 * @return
	 */
	public static String buildViolationMessage(ConstraintViolationException v) {
		return buildViolationMessage(v.getConstraintViolations());
	}
	
	/**
	 * 构造报错信息
	 * @param v
	 * @return
	 */
	public static String buildViolationMessage(Collection<ConstraintViolation<?>> v) {
		StringBuilder sb = new StringBuilder();
		sb.append("参数错误:");
		for (ConstraintViolation<?> item : v) {
			sb.append("[");
			sb.append(buildMessage(item.getPropertyPath()));
			sb.append("]" + item.getMessage());
			sb.append(", ");
		}
		return sb.toString();
	}
	
	/**
	 * 构造报错信息
	 * @param v
	 * @return
	 */
	public static String buildViolationMessage(ConstraintViolation<?> v) {
		StringBuilder sb = new StringBuilder();
		sb.append("参数错误:");
		sb.append(buildMessage(v.getPropertyPath()));
		sb.append(" " + v.getMessage());
		return sb.toString();
	}

	/**
	 * 构造报错信息
	 * @param path
	 * @return
	 */
	private static String buildMessage(Path path) {
		ArrayList<String> list = new ArrayList<String>();
		for (Path.Node node : path) {
			list.add(node.getName());
		}
		return StringUtils.join(list.iterator(), ".");
	}

}
