package com.wzp.util.valid;

/**
 * 用于验证的常用正则表达式
 */
public class ValidationPatterns {
	
	/**
	 * 实体名称,不允许输入 / \ : * ? % & ' " $ &lt; &gt; 的特殊符号
	 */
	public static final String ENTITY_NAME = "[^/\\:*?%&'\"$<>]+$";

	/**
	 * 手机号
	 */
	public static final String CELL_PHONE_NUMBER = "^1\\d{10}$";
	
	/**
	 * 身份证,15或18位数字及字符x
	 */
	public static final String PRC_ID_NUMBER = "(^[\\d]{15}$)|(^[\\d]{17}[\\dxX]$)";
}
