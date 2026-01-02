package com.hrplatform.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileUtil {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",                       // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    );


    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "jpg", "jpeg", "png", "doc", "docx"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // CHANGED: 10MB in bytes (was 30MB)

    public static boolean isValidFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || originalFilename == null) {
            return false;
        }

        // Check content type
        boolean validContentType = ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());

        // Check file extension
        String extension = getFileExtension(originalFilename);
        boolean validExtension = ALLOWED_EXTENSIONS.contains(extension.toLowerCase());

        return validContentType && validExtension;
    }

    public static boolean isValidFileSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        return file.getSize() <= MAX_FILE_SIZE;
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }

        return filename.substring(lastIndexOf + 1);
    }

    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return filename;
        }

        return filename.substring(0, lastIndexOf);
    }

    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        // Remove special characters and replace spaces with underscores
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();
    }

    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }

    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public static String getMaxFileSizeFormatted() {
        return formatFileSize(MAX_FILE_SIZE);
    }

    public static boolean isPdf(MultipartFile file) {
        return file != null && "application/pdf".equals(file.getContentType());
    }

    public static boolean isImage(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType().toLowerCase();

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") || // .doc
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"); // .docx

    }
}