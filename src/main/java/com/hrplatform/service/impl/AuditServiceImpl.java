package com.hrplatform.service.impl;

import com.hrplatform.dto.response.AuditLogResponse;
import com.hrplatform.entity.CloudinaryUploadLog;
import com.hrplatform.entity.DocumentConfigLog;
import com.hrplatform.entity.HrActivityLog;
import com.hrplatform.entity.StaffSubmissionLog;
import com.hrplatform.mapper.AuditLogMapper;
import com.hrplatform.mapper.CloudinaryUploadLogMapper;
import com.hrplatform.repository.*;
import com.hrplatform.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final StaffSubmissionLogRepository staffSubmissionLogRepository;
    private final HrActivityLogRepository hrActivityLogRepository;
    private final DocumentConfigLogRepository documentConfigLogRepository;
    private final CloudinaryUploadLogRepository cloudinaryUploadLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final CloudinaryUploadLogMapper cloudinaryUploadLogMapper;

    @Override
    @Async
    @Transactional
    public void logStaffValidation(String staffIdNumber, String departmentName, String details) {
        StaffSubmissionLog log = auditLogMapper.toStaffSubmissionLog(
                staffIdNumber,
                departmentName,
                "VALIDATION",
                details
        );
        staffSubmissionLogRepository.save(log);
    }

    @Override
    @Async
    @Transactional
    public void logUploadSuccess(String staffIdNumber, String departmentName,
                                 String documentName, String cloudinaryUrl) {

        StaffSubmissionLog submissionLog = auditLogMapper.toStaffSubmissionLog(
                staffIdNumber,
                departmentName,
                "UPLOAD_SUCCESS",
                "Document uploaded: " + documentName
        );
        staffSubmissionLogRepository.save(submissionLog);

        CloudinaryUploadLog uploadLog = cloudinaryUploadLogMapper.toEntity(
                staffIdNumber,
                documentName,
                departmentName,
                "SUCCESS",
                cloudinaryUrl,
                extractPublicId(cloudinaryUrl),
                null,
                0L
        );
        cloudinaryUploadLogRepository.save(uploadLog);
    }

    @Override
    @Async
    @Transactional
    public void logUploadFailure(String staffIdNumber, String departmentName,
                                 String documentName, String error) {

        StaffSubmissionLog submissionLog = auditLogMapper.toStaffSubmissionLog(
                staffIdNumber,
                departmentName,
                "UPLOAD_FAILED",
                "Failed to upload " + documentName + ": " + error
        );
        staffSubmissionLogRepository.save(submissionLog);

        CloudinaryUploadLog uploadLog = cloudinaryUploadLogMapper.toEntity(
                staffIdNumber,
                documentName,
                departmentName,
                "FAILED",
                null,
                null,
                error,
                0L
        );
        cloudinaryUploadLogRepository.save(uploadLog);
    }

    @Override
    @Async
    @Transactional
    public void logHrActivity(String hrUserEmail, String action,
                              String targetDepartment, String details) {

        HrActivityLog log = auditLogMapper.toHrActivityLog(
                hrUserEmail,
                action,
                targetDepartment,
                details
        );
        hrActivityLogRepository.save(log);
    }

    @Override
    @Async
    @Transactional
    public void logDocumentConfigChange(String hrUserEmail, String departmentName,
                                        String action, String documentName, String details) {

        DocumentConfigLog log = auditLogMapper.toDocumentConfigLog(
                hrUserEmail,
                departmentName,
                action,
                documentName,
                details
        );
        documentConfigLogRepository.save(log);
    }

    @Override
    @Async
    @Transactional
    public void logExport(String hrUserEmail, String departmentFilter, Integer totalRecords) {
        HrActivityLog log = auditLogMapper.toHrActivityLog(
                hrUserEmail,
                "EXPORT",
                departmentFilter,
                String.format("Exported %d records", totalRecords)
        );
        hrActivityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getHrActivityLogs(Pageable pageable) {
        Page<HrActivityLog> logs = hrActivityLogRepository.findAll(pageable);
        return logs.map(auditLogMapper::toAuditLogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getStaffSubmissionLogs(Pageable pageable) {
        Page<StaffSubmissionLog> logs = staffSubmissionLogRepository.findAll(pageable);
        return logs.map(auditLogMapper::toAuditLogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getDocumentConfigLogs(Pageable pageable) {
        Page<DocumentConfigLog> logs = documentConfigLogRepository.findAll(pageable);
        return logs.map(auditLogMapper::toAuditLogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getHrActivityLogsByDateRange(LocalDateTime startDate,
                                                               LocalDateTime endDate) {
        List<HrActivityLog> logs = hrActivityLogRepository.findByDateRange(startDate, endDate);
        return logs.stream()
                .map(auditLogMapper::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    private String extractPublicId(String cloudinaryUrl) {
        if (cloudinaryUrl == null) return null;

        try {
            String[] parts = cloudinaryUrl.split("/");
            String lastPart = parts[parts.length - 1];
            return lastPart.split("\\.")[0];
        } catch (Exception e) {
            return null;
        }
    }
}