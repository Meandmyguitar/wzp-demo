package com.wzp.util.etc;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

/**
 * 隐式比较器，支持比较不同类的实例之间是否相等，例如字符串的"1"和数字1
 * <p>
 * <pre>
 *   // 字符串和其他类型
 *   assert compare(1, "1") == true;
 *   // BigDecimal和其他类型
 *   assert compare(new BigDecimal("1.00"), "1") == true;
 *   // 浮点和其他类型
 *   assert compare(1.0, "1") == true;
 *   // 枚举和其他类型
 *   assert compare(AEnum.EnumValue, "EnumValue") == true;
 *   // 集合和单个对象
 *   assert compare(Arrays.asList(1), 1) == true;
 * </pre>
 * </p>
 * @author lmind
 *
 */
public class ImplicitComparer {
	
	public static boolean compare(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.equals(b)) {
			return true;
		}
		if (compare0(a, b)) {
			return true;
		}
		if (compare0(b, a)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static boolean compare0(Object a, Object b) {
		
		if (a instanceof CharSequence) {
			if (b instanceof Number) {
				return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
			}
		} else if (a instanceof BigDecimal) {
			if (b instanceof Number) {
				return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
			}
		} else if (a instanceof Number) {
			if (b instanceof Number) {
				return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
			}
		} else if (a instanceof Enum) {
			return compare(((Enum)a).name(), b);
		} else if (a instanceof Collection && ((Collection<?>)a).size() == 1) {
			return compare(((Collection<?>)a).iterator().next(), b);
		} else if (a instanceof Collection && b instanceof Collection) {
			return compare((Collection)a, (Collection)b);
		}
		
		return false;
	}
	
	private static boolean compare(Collection<?> a, Collection<?> b) {
		if (a.size() != b.size()) {
			return false;
		}
		Iterator<?> a1 = a.iterator();
		Iterator<?> b1 = b.iterator();
		while(a1.hasNext()) {
			if (!compare(a1.next(), b1.next())) {
				return false;
			}
		}
		return true;
	}
}
