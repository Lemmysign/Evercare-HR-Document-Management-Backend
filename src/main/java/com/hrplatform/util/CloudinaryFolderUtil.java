package com.hrplatform.util;

import java.time.Year;

public class CloudinaryFolderUtil {

    private static final String BASE_FOLDER = "staff_documents";

    public static String generateDepartmentFolder(String departmentName) {
        String sanitizedDepartment = sanitizeFolderName(departmentName);
        int currentYear = Year.now().getValue();

        return String.format("%s/%s/%d", BASE_FOLDER, sanitizedDepartment, currentYear);
    }

    public static String generateDepartmentFolder(String departmentName, int year) {
        String sanitizedDepartment = sanitizeFolderName(departmentName);

        return String.format("%s/%s/%d", BASE_FOLDER, sanitizedDepartment, year);
    }

    public static String generatePublicId(String staffIdNumber, String fileName) {
        String sanitizedStaffId = sanitizeString(staffIdNumber);
        String sanitizedFileName = sanitizeString(FileUtil.getFileNameWithoutExtension(fileName));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);

        return String.format("%s_%s_%s", sanitizedStaffId, sanitizedFileName, timestamp);
    }

    public static String sanitizeFolderName(String folderName) {
        if (folderName == null || folderName.isEmpty()) {
            return "default";
        }

        return folderName.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_-]", "")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

    private static String sanitizeString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

    public static String extractDepartmentFromFolder(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return null;
        }

        String[] parts = folderPath.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }

        return null;
    }

    public static Integer extractYearFromFolder(String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return null;
        }

        String[] parts = folderPath.split("/");
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}