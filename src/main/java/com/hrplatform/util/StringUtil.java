package com.hrplatform.util;

import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String capitalizeWords(String str) {
        if (isEmpty(str)) {
            return str;
        }

        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(capitalize(word)).append(" ");
            }
        }

        return result.toString().trim();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength) + "...";
    }

    public static String removeSpecialCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static String toSnakeCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        String[] words = str.split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0].toLowerCase());

        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i]));
        }

        return result.toString();
    }

    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return email;
        }

        String masked = username.charAt(0) +
                "*".repeat(username.length() - 2) +
                username.charAt(username.length() - 1);

        return masked + "@" + domain;
    }

    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }

        return result.toString();
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }

        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    public static String normalizeSpaces(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.trim().replaceAll("\\s+", " ");
    }
}