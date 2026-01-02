package com.hrplatform.service;

import com.hrplatform.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    void logStaffValidation(String staffIdNumber, String departmentName, String details);

    void logUploadSuccess(String staffIdNumber, String departmentName, String documentName, String cloudinaryUrl);

    void logUploadFailure(String staffIdNumber, String departmentName, String documentName, String error);

    void logHrActivity(String hrUserEmail, String action, String targetDepartment, String details);

    void logDocumentConfigChange(String hrUserEmail, String departmentName, String action, String documentName, String details);

    void logExport(String hrUserEmail, String departmentFilter, Integer totalRecords);

    Page<AuditLogResponse> getHrActivityLogs(Pageable pageable);

    Page<AuditLogResponse> getStaffSubmissionLogs(Pageable pageable);

    Page<AuditLogResponse> getDocumentConfigLogs(Pageable pageable);

    List<AuditLogResponse> getHrActivityLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}