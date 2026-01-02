package com.hrplatform.mapper;

import com.hrplatform.dto.response.StaffValidationResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StaffMapper {

    public StaffValidationResponse toValidationResponse(Staff staff, Boolean isValid, String message) {
        return StaffValidationResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                // FIXED: Handle null department safely
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
                // FIXED: Handle null department safely
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
                // FIXED: Handle null department safely
                .departmentName(staff.getDepartment() != null ? staff.getDepartment().getName() : "Not Assigned")
                .documents(documentInfos)
                .build();
    }

    private SubmissionDetailsResponse.DocumentSubmissionInfo toDocumentSubmissionInfo(DocumentSubmission submission) {
        return SubmissionDetailsResponse.DocumentSubmissionInfo.builder()
                .submissionId(submission.getId())
                .documentName(submission.getDocumentRequirement().getDocumentName())
                .cloudinaryUrl(submission.getCloudinaryUrl())
                .fileName(submission.getFileName())
                .fileSize(submission.getFileSize())
                .uploadedAt(submission.getCreatedAt())
                .build();
    }
}