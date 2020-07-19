package com.wzp.util.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class TimeUtils {

    /**
     * 默认的ZoneOffset
     */
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = OffsetDateTime.now().getOffset();

    /**
     * LocalDateTime转换为Date对象
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(DEFAULT_ZONE_OFFSET));
    }

    /**
     * 获取月度第一天
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * 获取月度最后一天
     */
    public static LocalDate lastOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate dayOfWeek(LocalDate date) {
        return date.with(ChronoField.DAY_OF_WEEK, 1);
    }
}
