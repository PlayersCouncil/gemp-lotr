package com.gempukku.lotro.common;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {
    public static final ZoneId UTC = ZoneOffset.UTC;

    public static final DateTimeFormatter DateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter IntDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter APIDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    public static String FormatDateTime(ZonedDateTime dateTime) { return dateTime.format(DateTimeFormat); }

    public static String FormatTime(ZonedDateTime dateTime) { return dateTime.format(TimeFormat); }
    public static String FormatDate(ZonedDateTime date) { return date.format(DateFormat); }
    public static ZonedDateTime ParseDate(LocalDateTime time) { return ZonedDateTime.of(time, UTC); }

    public static ZonedDateTime ParseDate(LocalDate date) { return ZonedDateTime.of(date, LocalTime.MIDNIGHT,  UTC); }
    public static ZonedDateTime ParseDate(int time) { return LocalDate.parse(String.valueOf(time), IntDateFormat).atStartOfDay(ZoneOffset.UTC); }
    public static ZonedDateTime ParseDate(String time) { return LocalDateTime.parse(time, APIDateTimeFormat).atZone(UTC); }
    public static ZonedDateTime MinDate() { return DateOf(2012, 1, 1); }
    public static ZonedDateTime Now() { return ZonedDateTime.now(UTC); }
    public static ZonedDateTime Today() { return Now().truncatedTo(ChronoUnit.DAYS); }
    public static ZonedDateTime DateOf(int year, int month, int day) { return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, UTC); }

    public static boolean IsToday(ZonedDateTime date) { return DaysSince(date) == 0; }
    public static boolean IsAtLeastDayAfter(ZonedDateTime a, ZonedDateTime b) { return DaysBetween(b, a) >= 1; }
    public static boolean IsAfterStart(ZonedDateTime date, ZonedDateTime start) { return date.isEqual(start) || date.isAfter(start); }
    public static boolean IsBeforeEnd(ZonedDateTime date, ZonedDateTime end) { return DaysBetween(end, date) < 0; }
    public static boolean IsSameDay(ZonedDateTime a, ZonedDateTime b) { return DaysBetween(a, b) == 0; }
    public static long DaysBetween(ZonedDateTime a, ZonedDateTime b) { return Duration.between(a, b).toDays(); }
    public static long DaysSince(ZonedDateTime date) { return DaysBetween(date, Now()); }

    public static String HumanDuration(Duration timespan) {
        return DurationFormatUtils.formatDurationWords(timespan.toMillis(), true, true);
    }

    public static ZonedDateTime ParseStringDate(String str) {
        return ZonedDateTime.of(LocalDateTime.parse(str, DateTimeFormat), DateUtils.UTC);
    }

    public static int getCurrentDate() {
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        return date.get(Calendar.YEAR) * 10000 + (date.get(Calendar.MONTH) + 1) * 100 + date.get(Calendar.DAY_OF_MONTH);
    }

    public static int getCurrentMinute() {
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        return date.get(Calendar.YEAR) * 100000000 + (date.get(Calendar.MONTH) + 1) * 1000000 + date.get(Calendar.DAY_OF_MONTH) * 10000 + date.get(Calendar.HOUR_OF_DAY) * 100 + date.get(Calendar.MINUTE);
    }

    public static String getStringDateWithHour() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(new Date());
    }

    public static String formatDateWithHour(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public static Date parseDateWithHour(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.parse(date);
    }

    public static int offsetDate(int start, int dayOffset) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date date = format.parse(String.valueOf(start));
            date.setDate(date.getDate() + dayOffset);
            return Integer.parseInt(format.format(date));
        } catch (ParseException exp) {
            throw new RuntimeException("Can't parse date", exp);
        }
    }

    public static int getMondayBeforeOrOn(int date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date current = format.parse(String.valueOf(date));
            if (current.getDay() == 0)
                return offsetDate(date, -6);
            else
                return offsetDate(date, 1 - current.getDay());
        } catch (ParseException exp) {
            throw new RuntimeException("Can't parse date", exp);
        }
    }
}
