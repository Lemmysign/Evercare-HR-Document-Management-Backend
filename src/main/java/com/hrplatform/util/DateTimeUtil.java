package com.hrplatform.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_ONLY_FORMATTER);
    }

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_ONLY_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER);
    }

    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }

    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.DAYS)
                .plusDays(1)
                .minusNanos(1);
    }

    public static long getDaysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    public static boolean isInRange(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);

        if (seconds < 60) {
            return seconds + " seconds ago";
        }

        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + " hours ago";
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 30) {
            return days + " days ago";
        }

        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) {
            return months + " months ago";
        }

        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " years ago";
    }
}