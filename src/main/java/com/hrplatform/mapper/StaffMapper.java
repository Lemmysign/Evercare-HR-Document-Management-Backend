package com.hrplatform.mapper;

import com.hrplatform.dto.response.StaffValidationResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StaffMapper {

    // WINDOWS LOCALHOST - ACTIVE
    @Value("${app.base-url:http://localhost:9090}")
    private String baseUrl;

    // LINUX SERVER - Uncomment when deploying
    // @Value("${app.base-url:https://www.hrpo.ec.ng}")
    // private String baseUrl;

    public StaffValidationResponse toValidationResponse(Staff staff, Boolean isValid, String message) {
        return StaffValidationResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .departmentId(staff.getDepartment() != null ? staff.getDepartment().getId() : null)
                .departmentName(staff.getDepartment() != null ? staff.getDepartment().getName() : null)
                .isValid(isValid)
                .hasDepartment(staff.getDepartment() != null)
                .message(message)
                .availableDepartments(null) // Will be set by service layer if needed
                .build();
    }

    public SubmissionListResponse toSubmissionListResponse(Staff staff, Long documentsSubmitted) {
        return SubmissionListResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .departmentName(staff.getDepartment() != null ? staff.getDepartment().getName() : "Not Assigned")
                .documentsSubmitted(documentsSubmitted)
                .hasSubmissions(documentsSubmitted > 0)
                .build();
    }

    public SubmissionDetailsResponse toSubmissionDetailsResponse(Staff staff, List<DocumentSubmission> submissions) {
        List<SubmissionDetailsResponse.DocumentSubmissionInfo> documentInfos = submissions.stream()
                .map(this::toDocumentSubmissionInfo)
                .collect(Collectors.toList());

        return SubmissionDetailsResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .departmentName(staff.getDepartment() != null ? staff.getDepartment().getName() : "Not Assigned")
                .documents(documentInfos)
                .build();
    }

    /**
     * UPDATED: Maps DocumentSubmission to DocumentSubmissionInfo
     * Constructs full file URL from relative path stored in database
     */
    private SubmissionDetailsResponse.DocumentSubmissionInfo toDocumentSubmissionInfo(DocumentSubmission submission) {
        // CHANGED: Build full URL from filePath instead of using cloudinaryUrl
        String fileUrl = buildFileUrl(submission.getFilePath());

        return SubmissionDetailsResponse.DocumentSubmissionInfo.builder()
                .submissionId(submission.getId())
                .documentName(submission.getDocumentRequirement().getDocumentName())
                .cloudinaryUrl(fileUrl)  // CHANGED: Now contains local server URL
                .fileName(submission.getFileName())
                .fileSize(submission.getFileSize())
                .uploadedAt(submission.getCreatedAt())
                .build();
    }

    /**
     * Build full file URL from relative path
     *
     * WINDOWS LOCALHOST:
     *   Input:  "engineering/john_doe_resume_12345.pdf"
     *   Output: "http://localhost:9090/api/files/view?path=engineering/john_doe_resume_12345.pdf"
     *
     * LINUX SERVER:
     *   Input:  "engineering/john_doe_resume_12345.pdf"
     *   Output: "https://www.hrpo.ec.ng/api/files/view?path=engineering/john_doe_resume_12345.pdf"
     */
    private String buildFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            log.warn("⚠️ File path is null or empty");
            return null;
        }

        // URL encode the path to handle special characters
        String encodedPath = encodeUrlPath(filePath);

        // Construct full URL
        String fullUrl = String.format("%s/api/files/view?path=%s", baseUrl, encodedPath);

        log.debug("📎 Built file URL: {} -> {}", filePath, fullUrl);

        return fullUrl;
    }

    /**
     * Build download URL (alternative method if needed)
     */
    @SuppressWarnings("unused")
    private String buildDownloadUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        String encodedPath = encodeUrlPath(filePath);
        return String.format("%s/api/files/download?path=%s", baseUrl, encodedPath);
    }

    /**
     * URL encode path to handle special characters, spaces, etc.
     */
    private String encodeUrlPath(String path) {
        if (path == null) {
            return "";
        }

        try {
            // Don't encode the forward slashes (directory separators)
            // Only encode special characters
            return path
                    .replace(" ", "%20")
                    .replace("#", "%23")
                    .replace("&", "%26")
                    .replace("+", "%2B");
        } catch (Exception e) {
            log.error("❌ Failed to encode URL path: {}", path, e);
            return path;
        }
    }
}

/*
 * APPLICATION.PROPERTIES UPDATE NEEDED:
 *
 * Add this configuration:
 *
 * # WINDOWS LOCALHOST - ACTIVE
 * app.base-url=http://localhost:9090
 *
 * # LINUX SERVER - Uncomment when deploying
 * # app.base-url=https://www.hrpo.ec.ng
 *
 *
 * MIGRATION NOTES:
 *
 * 1. The field name "cloudinaryUrl" is kept in DTO for backward compatibility
 * 2. The actual value now contains your server URL, not Cloudinary
 * 3. Frontend code doesn't need changes - it still receives a full URL
 * 4. Just switch the base-url property when deploying to production
 *
 *
 * EXAMPLE OUTPUT:
 *
 * {
 *   "submissionId": "550e8400-e29b-41d4-a716-446655440000",
 *   "documentName": "Employee Resume",
 *   "cloudinaryUrl": "https://www.hrpo.ec.ng/api/files/view?path=engineering/john_doe_resume_12345.pdf",
 *   "fileName": "john_doe_resume.pdf",
 *   "fileSize": 2458624,
 *   "uploadedAt": "2026-01-10T14:30:00"
 * }
 */