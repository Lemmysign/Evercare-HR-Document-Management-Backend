package com.hrplatform.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtil.isEmpty(phoneNumber)) {
            return false;
        }

        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean isValidStaffId(String staffId) {
        if (StringUtil.isEmpty(staffId)) {
            return false;
        }

        // Staff ID should be alphanumeric and between 3-20 characters
        return staffId.length() >= 3 &&
                staffId.length() <= 20 &&
                ALPHANUMERIC_PATTERN.matcher(staffId).matches();
    }

    public static boolean isValidPassword(String password) {
        if (StringUtil.isEmpty(password)) {
            return false;
        }

        // Password must be at least 8 characters
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        // Password must contain at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Password must contain at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Password must contain at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        return true;
    }

    public static String getPasswordStrength(String password) {
        if (StringUtil.isEmpty(password)) {
            return "Empty";
        }

        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        if (strength <= 2) return "Weak";
        if (strength <= 4) return "Medium";
        return "Strong";
    }

    public static boolean isValidUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return false;
        }

        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNumeric(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        }

        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isAlphanumeric(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        }

        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}