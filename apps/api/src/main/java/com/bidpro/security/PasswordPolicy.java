package com.bidpro.security;

import java.nio.charset.StandardCharsets;

/** BCrypt limits input bytes, not Java characters. Never silently truncate credentials. */
public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static boolean fitsBcrypt(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static void validateNewPassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8 || !fitsBcrypt(password)) {
            throw new IllegalArgumentException("Use a password of at least 8 characters and at most 72 UTF-8 bytes");
        }
    }
}
