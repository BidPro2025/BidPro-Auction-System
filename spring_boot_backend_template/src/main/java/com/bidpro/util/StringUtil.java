package com.bidpro.util;

public class StringUtil {
    /**
     * Checks if a string is null or empty after trimming.
     * @param str The string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a string is not null and not empty after trimming.
     * @param str The string to check
     * @return true if not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Generates a random UUID string.
     * @return A random UUID as a string
     */
    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}