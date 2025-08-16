package com.bidpro.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a LocalDateTime to a string using the default pattern (yyyy-MM-dd HH:mm:ss).
     * @param dateTime The LocalDateTime to format
     * @return Formatted string or null if input is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Parses a string to LocalDateTime using the default pattern (yyyy-MM-dd HH:mm:ss).
     * @param dateTimeStr The string to parse
     * @return Parsed LocalDateTime or null if input is invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the current date and time.
     * @return Current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}