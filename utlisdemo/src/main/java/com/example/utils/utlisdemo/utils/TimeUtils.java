package com.example.utils.utlisdemo.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;

public final class TimeUtils {

    /**
     * 获取默认时间格式: yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = TimeFormat.LONG_DATE_PATTERN_LINE.formatter;

    private TimeUtils() {
        // no construct function
    }

    public static long betweenTwoDayAbs(String start, String end, TimeFormat format) {
        return Math.abs(LocalDate.parse(start, format.formatter).toEpochDay() - LocalDate.parse(end, format.formatter).toEpochDay());
    }

    public static long betweenTwoDay(String start, String end, TimeFormat format) {
        return LocalDate.parse(start, format.formatter).toEpochDay() - LocalDate.parse(end, format.formatter).toEpochDay();
    }

    public static void main(String[] args) {
        System.out.println(betweenTwoDayAbs("20190408", "20181018", TimeFormat.SHORT_DATE_PATTERN_NONE));
    }

    /**
     * String 转时间
     *
     * @param timeStr
     * @return
     */
    public static LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * String 转时间
     *
     * @param timeStr
     * @param format  时间格式
     * @return
     */
    public static LocalDateTime parseTime(String timeStr, TimeFormat format) {
        return LocalDateTime.parse(timeStr, format.formatter);
    }

    /**
     * 时间转 String
     *
     * @param time
     * @return
     */
    public static String parseTime(LocalDateTime time) {
        return DEFAULT_DATETIME_FORMATTER.format(time);
    }

    /**
     * 时间转 String
     *
     * @param time
     * @param format 时间格式
     * @return
     */
    public static String parseTime(LocalDateTime time, TimeFormat format) {
        return format.formatter.format(time);
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentDatetime() {
        return DEFAULT_DATETIME_FORMATTER.format(LocalDateTime.now());
    }

    /**
     * 获取当前时间
     *
     * @param format 时间格式
     * @return
     */
    public static String getCurrentDatetime(TimeFormat format) {
        return format.formatter.format(LocalDateTime.now());
    }

    /**
     * 返回当前时间+days天数
     *
     * @param format
     * @param days
     * @return string类型的时间
     */
    public static String getCurrentDateTimePlusDays(TimeFormat format, long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.plus(days, ChronoUnit.DAYS);
        return finalTime.format(format.formatter);
    }

    /**
     * 返回当前时间+days天数
     *
     * @param format
     * @param days
     * @return string类型的时间
     */
    public static String getCurrentDatePlusDaysWithCustomTime(String time, TimeFormat format, long days) {
        LocalDate now = LocalDate.parse(time, format.formatter);
        LocalDate finalTime = now.plus(days, ChronoUnit.DAYS);
        return finalTime.format(format.formatter);
    }

    /**
     * 返回当前时间+days天数
     *
     * @param oriformat
     * @param showformat
     * @return string类型的时间
     */
    public static String getCurrentDateWithCustomTime(String time, TimeFormat oriformat, TimeFormat showformat) {
        LocalDate now = LocalDate.parse(time, oriformat.formatter);
        return now.format(showformat.formatter);
    }

    /**
     * 返回当前时间+anytime的时间
     *
     * @param format
     * @param time
     * @param unit   时间单位
     * @return
     */
    public static String getCurrentDateTimePlusTime(TimeFormat format, long time, TemporalUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.plus(time, unit);
        return finalTime.format(format.formatter);
    }

    /**
     * 当前时间的-days
     *
     * @param format
     * @param days
     * @return
     */
    public static String getCurrentDateTimeMinusDays(TimeFormat format, long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minusDays(days);
        return finalTime.format(format.formatter);
    }

    /**
     * 定制时间的-days
     *
     * @param time
     * @param format
     * @param days
     * @return
     */
    public static String getCurrentDateMinusDaysWithCustomTime(String time, TimeFormat format, long days) {
        LocalDate now = LocalDate.parse(time, format.formatter);
        LocalDate finalTime = now.minusDays(days);
        return finalTime.format(format.formatter);
    }

    /**
     * 当前时间的提前anytime
     *
     * @param format
     * @param time
     * @param unit
     * @return
     */
    public static String getCurrentDateTimeMinusTime(TimeFormat format, long time, TemporalUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minus(time, unit);
        return finalTime.format(format.formatter);
    }

    /**
     * 得到前当月的最后一天
     *
     * @return
     */
    public static String getLastDayOfCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        //本月的最后一天
        LocalDateTime lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
        return lastDay.format(TimeFormat.SHORT_DATE_PATTERN_LINE.formatter);
    }

    /**
     * 得到前n个月的第一天
     *
     * @param
     * @return
     */
    public static String getFirstDayOfBeforeMonth(int time) {

        LocalDate now = LocalDate.now();
        LocalDate finalTime = LocalDate.of(now.getYear(), Month.of(now.getMonth().getValue() - time), 1);
        return finalTime.format(TimeFormat.SHORT_DATE_PATTERN_LINE.formatter);
    }

    /**
     * 得到前n个月的第一天
     *
     * @param format 格式化日期
     * @return
     */
    public static String getFirstDayOfBeforeMonth(int time, TimeFormat format) {
        LocalDate now = LocalDate.now();
        return now.minusMonths((long) time).withDayOfMonth(1).format(format.formatter);
    }

    /**
     * 得到当前月的第一天
     *
     * @param
     * @return
     */
    public static String getFirstDayOfCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minusDays(now.getDayOfMonth() - 1);
        return finalTime.format(TimeFormat.SHORT_DATE_PATTERN_LINE.formatter);
    }

    /**
     * 得到当前月的第一天
     *
     * @param format 格式化日期
     * @return
     */
    public static String getFirstDayOfCurrentMonth(TimeFormat format) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minusDays(now.getDayOfMonth() - 1);
        return finalTime.format(format.formatter);
    }

    /**
     * 得到当前周的星期一的日期
     *
     * @return
     */
    public static String getFirstDayOfCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return finalTime.format(TimeFormat.SHORT_DATE_PATTERN_LINE.formatter);
    }

    /**
     * 得到当前周的星期一的日期
     *
     * @return
     */
    public static String getFirstDayOfCurrentWeek(TimeFormat format) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalTime = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return finalTime.format(format.formatter);
    }

    /**
     * 获取当前日期是星期几
     *
     * @param time
     * @param format
     * @return
     */
    public static int getWeekNumOfCurrentDay(String time, TimeFormat format) {
        LocalDate parse = LocalDate.parse(time, format.formatter);
        return parse.getDayOfWeek().getValue();
    }


    /**
     * 时间格式
     */
    public enum TimeFormat {

        /**
         * 短时间格式
         */
        SHORT_DATE_PATTERN_LINE("yyyy-MM-dd"),
        SHORT_DATE_PATTERN_SLASH("yyyy/MM/dd"),
        SHORT_DATE_PATTERN_DOUBLE_SLASH("yyyy\\MM\\dd"),
        SHORT_DATE_PATTERN_NONE("yyyyMMdd"),

        /**
         * 长时间格式
         */
        LONG_DATE_PATTERN_LINE("yyyy-MM-dd HH:mm:ss"),
        LONG_DATE_PATTERN_SLASH("yyyy/MM/dd HH:mm:ss"),
        LONG_DATE_PATTERN_DOUBLE_SLASH("yyyy\\MM\\dd HH:mm:ss"),
        LONG_DATE_PATTERN_NONE("yyyyMMdd HH:mm:ss"),

        /**
         * 长时间格式 带毫秒
         */
        LONG_DATE_PATTERN_WITH_MILLIS_LINE("yyyy-MM-dd HH:mm:ss.SSS"),
        LONG_DATE_PATTERN_WITH_MILLIS_SLASH("yyyy/MM/dd HH:mm:ss.SSS"),
        LONG_DATE_PATTERN_WITH_MILLIS_DOUBLE_SLASH("yyyy\\MM\\dd HH:mm:ss.SSS"),
        LONG_DATE_PATTERN_WITH_MILLIS_NONE("yyyyMMdd HH:mm:ss.SSS");

        private transient DateTimeFormatter formatter;

        TimeFormat(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }
    }
}