package com.wzp.util.etc;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	/**
	 * 用特定的字符号进行遮掩
	 * 
	 * @param value
	 *            原始字符串
	 * @param prefix
	 *            前几个字符不会被遮掩
	 * @param suffix
	 *            后几个字符不会被遮掩
	 * @return
	 */
	public static String mask(String value, int prefix, int suffix, char maskChar) {
		if (prefix + suffix > value.length()) {
			throw new IllegalArgumentException("字符串长度不够");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(value.substring(0, prefix));
		for (int i = prefix + suffix; i < value.length(); i++) {
			sb.append(maskChar);
		}
		sb.append(value.substring(value.length() - suffix));
		return sb.toString();
	}
	
	
	/**
	 * 将字符串按照charset编码后，返回不超出maxBytes字节长度的结果
	 * <p>
	 * 例如collapseByCharset("中文信息", Charset.forName("utf-8"),
	 * 10)返回"中文信",因为utf-8一个中文字符占用3字节
	 * </p>
	 * 
	 * @param str
	 *            源字符串
	 * @param charset
	 * @param maxBytes
	 * @return
	 */
	public static String collapseByCharset(String str, Charset charset, int maxBytes) {

		byte[] buf = new byte[maxBytes];
		ByteBuffer out = ByteBuffer.wrap(buf);
		CharBuffer in = CharBuffer.wrap(str);
		CharsetEncoder encoder = charset.newEncoder();

		encoder.encode(in, out, true);
		return str.substring(0, in.position());
	}
}
