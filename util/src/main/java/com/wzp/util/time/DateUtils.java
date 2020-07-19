package com.wzp.util.time;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	public static String format(String pattern, Date date) {
		return new SimpleDateFormat(pattern).format(date);
	}
	
	public static String format(String pattern, long date) {
		return format(pattern, new Date(date));
	}
}
