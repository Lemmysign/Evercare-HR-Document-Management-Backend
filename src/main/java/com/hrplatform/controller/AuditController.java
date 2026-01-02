package com.hrplatform.controller;

import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.AuditLogResponse;
import com.hrplatform.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit", description = "Audit log endpoints (HR Only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/hr-activity")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get HR activity logs", description = "Retrieve HR activity audit logs (HR only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getHrActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Fetching HR activity logs - page: {}, size: {}", page, size);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLogResponse> response = auditService.getHrActivityLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "HR activity logs retrieved successfully"));
    }

    @GetMapping("/staff-submission")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get staff submission logs", description = "Retrieve staff submission audit logs (HR only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getStaffSubmissionLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Fetching staff submission logs - page: {}, size: {}", page, size);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLogResponse> response = auditService.getStaffSubmissionLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "Staff submission logs retrieved successfully"));
    }

    @GetMapping("/document-config")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get document config logs", description = "Retrieve document configuration audit logs (HR only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getDocumentConfigLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Fetching document config logs - page: {}, size: {}", page, size);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLogResponse> response = auditService.getDocumentConfigLogs(pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "Document config logs retrieved successfully"));
    }

    @GetMapping("/hr-activity/date-range")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get HR activity logs by date range", description = "Retrieve HR activity logs within a date range (HR only)")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getHrActivityLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Fetching HR activity logs from {} to {}", startDate, endDate);

        List<AuditLogResponse> response = auditService.getHrActivityLogsByDateRange(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(response, "HR activity logs retrieved successfully"));
    }
}