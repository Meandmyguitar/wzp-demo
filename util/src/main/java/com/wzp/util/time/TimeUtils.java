package com.wzp.util.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Date;

public class TimeUtils {
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public TimeUtils() {
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(DEFAULT_ZONE_OFFSET));
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), DEFAULT_ZONE_OFFSET);
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate lastOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate dayOfWeek(LocalDate date) {
        return date.with(ChronoField.DAY_OF_WEEK, 1L);
    }
}
