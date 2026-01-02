package com.hrplatform.mapper;

import com.hrplatform.dto.response.AuditLogResponse;
import com.hrplatform.entity.DocumentConfigLog;
import com.hrplatform.entity.HrActivityLog;
import com.hrplatform.entity.StaffSubmissionLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public StaffSubmissionLog toStaffSubmissionLog(
            String staffIdNumber,
            String departmentName,
            String action,
            String details) {

        return StaffSubmissionLog.builder()
                .staffIdNumber(staffIdNumber)
                .departmentName(departmentName)
                .action(action)
                .details(details)
                .build();
    }

    public HrActivityLog toHrActivityLog(
            String hrUserEmail,
            String action,
            String targetDepartment,
            String details) {

        return HrActivityLog.builder()
                .hrUserEmail(hrUserEmail)
                .action(action)
                .targetDepartment(targetDepartment)
                .details(details)
                .build();
    }

    public DocumentConfigLog toDocumentConfigLog(
            String hrUserEmail,
            String departmentName,
            String action,
            String documentName,
            String details) {

        return DocumentConfigLog.builder()
                .hrUserEmail(hrUserEmail)
                .departmentName(departmentName)
                .action(action)
                .documentName(documentName)
                .details(details)
                .build();
    }

    public AuditLogResponse toAuditLogResponse(HrActivityLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getHrUserEmail())
                .action(log.getAction())
                .targetDepartment(log.getTargetDepartment())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .build();
    }

    public AuditLogResponse toAuditLogResponse(StaffSubmissionLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getStaffIdNumber())
                .action(log.getAction())
                .targetDepartment(log.getDepartmentName())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .build();
    }

    public AuditLogResponse toAuditLogResponse(DocumentConfigLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getHrUserEmail())
                .action(log.getAction())
                .targetDepartment(log.getDepartmentName())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .build();
    }
}