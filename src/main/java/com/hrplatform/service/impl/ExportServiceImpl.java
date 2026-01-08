package com.hrplatform.service.impl;

import com.hrplatform.dto.request.ExportFilterRequest;
import com.hrplatform.dto.response.ExportResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.mapper.ExportLogMapper;
import com.hrplatform.repository.DocumentSubmissionRepository;
import com.hrplatform.repository.ExportLogRepository;
import com.hrplatform.repository.StaffRepository;
import com.hrplatform.service.AuditService;
import com.hrplatform.service.DepartmentService;
import com.hrplatform.service.ExcelExportService;
import com.hrplatform.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final StaffRepository staffRepository;
    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final DepartmentService departmentService;
    private final ExcelExportService excelExportService;
    private final ExportLogRepository exportLogRepository;
    private final ExportLogMapper exportLogMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public ExportResponse exportToExcel(ExportFilterRequest request, String hrUserEmail) {
        log.info("Exporting data to Excel for user: {} with filters: departmentId={}, status={}, dateRange={} to {}",
                hrUserEmail, request.getDepartmentId(), request.getSubmissionStatus(),
                request.getDateRangeStart(), request.getDateRangeEnd());

        // Validate and fetch staff based on filters
        List<Staff> staffList = fetchStaffByFilters(request);

        if (staffList.isEmpty()) {
            log.warn("No staff found matching the filter criteria");
        }

        // Fetch all submissions for these staff
        List<DocumentSubmission> allSubmissions = fetchSubmissionsByFilters(request, staffList);

        log.info("Found {} staff and {} submissions matching filters", staffList.size(), allSubmissions.size());

        // Generate Excel file
        byte[] excelData = excelExportService.generateExcelFile(staffList, allSubmissions);

        // Generate filename
        String fileName = generateFileName(request);

        // Log export
        String departmentName = getDepartmentNameForLog(request);

        exportLogRepository.save(
                exportLogMapper.toEntity(hrUserEmail, request, staffList.size(), departmentName)
        );

        auditService.logHrActivity(
                hrUserEmail,
                "EXPORT",
                departmentName,
                String.format("Exported %d staff records with %d submissions", staffList.size(), allSubmissions.size())
        );

        log.info("Export completed successfully. Total records: {}", staffList.size());

        return ExportResponse.builder()
                .fileName(fileName)
                .fileData(excelData)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .totalRecords(staffList.size())
                .message(String.format("Export completed successfully. %d staff records exported.", staffList.size()))
                .build();
    }

    private List<Staff> fetchStaffByFilters(ExportFilterRequest request) {
        List<Staff> staffList;

        // Filter by department
        if (request.getDepartmentId() != null) {
            log.info("Filtering by department ID: {}", request.getDepartmentId());
            Department department = departmentService.findById(request.getDepartmentId());

            staffList = staffRepository.findAll().stream()
                    .filter(staff -> staff.getDepartment() != null &&
                            staff.getDepartment().getId().equals(request.getDepartmentId()))
                    .collect(Collectors.toList());
        } else {
            log.info("Fetching all staff (no department filter)");
            staffList = staffRepository.findAll();
        }

        // Apply submission status filter
        if (request.getSubmissionStatus() != null &&
                !request.getSubmissionStatus().equalsIgnoreCase("ALL")) {

            if ("SUBMITTED".equalsIgnoreCase(request.getSubmissionStatus())) {
                log.info("Filtering for staff WITH submissions");
                staffList = staffList.stream()
                        .filter(staff -> documentSubmissionRepository.countByStaffId(staff.getId()) > 0)
                        .collect(Collectors.toList());
            } else if ("UNSUBMITTED".equalsIgnoreCase(request.getSubmissionStatus())) {
                log.info("Filtering for staff WITHOUT submissions");
                staffList = staffList.stream()
                        .filter(staff -> documentSubmissionRepository.countByStaffId(staff.getId()) == 0)
                        .collect(Collectors.toList());
            }
        }

        return staffList;
    }

    private List<DocumentSubmission> fetchSubmissionsByFilters(ExportFilterRequest request, List<Staff> staffList) {
        List<DocumentSubmission> submissions;

        // Apply date range filter if provided
        if (request.getDateRangeStart() != null && request.getDateRangeEnd() != null) {
            log.info("Filtering submissions by date range: {} to {}",
                    request.getDateRangeStart(), request.getDateRangeEnd());

            if (request.getDepartmentId() != null) {
                submissions = documentSubmissionRepository.findByDepartmentIdAndDateRange(
                        request.getDepartmentId(),
                        request.getDateRangeStart(),
                        request.getDateRangeEnd()
                );
            } else {
                submissions = documentSubmissionRepository.findByDateRange(
                        request.getDateRangeStart(),
                        request.getDateRangeEnd()
                );
            }
        } else {
            // No date filter - fetch all submissions
            if (request.getDepartmentId() != null) {
                submissions = documentSubmissionRepository.findByDepartmentId(request.getDepartmentId());
            } else {
                submissions = documentSubmissionRepository.findAll();
            }
        }

        // Filter submissions to only include staff in the filtered list
        List<UUID> staffIds = staffList.stream()
                .map(Staff::getId)
                .collect(Collectors.toList());

        submissions = submissions.stream()
                .filter(submission -> staffIds.contains(submission.getStaff().getId()))
                .collect(Collectors.toList());

        log.info("Filtered submissions count: {}", submissions.size());

        return submissions;
    }

    private String getDepartmentNameForLog(ExportFilterRequest request) {
        if (request.getDepartmentId() != null) {
            try {
                return departmentService.findById(request.getDepartmentId()).getName();
            } catch (ResourceNotFoundException e) {
                return "Unknown Department";
            }
        }
        return "All Departments";
    }

    private String generateFileName(ExportFilterRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        String departmentPart;
        if (request.getDepartmentId() != null) {
            try {
                departmentPart = departmentService.findById(request.getDepartmentId())
                        .getName()
                        .replaceAll("\\s+", "_");
            } catch (ResourceNotFoundException e) {
                departmentPart = "UnknownDept";
            }
        } else {
            departmentPart = "AllDepartments";
        }

        String statusPart = "";
        if (request.getSubmissionStatus() != null &&
                !request.getSubmissionStatus().equalsIgnoreCase("ALL")) {
            statusPart = "_" + request.getSubmissionStatus();
        }

        return String.format("Staff_Documents_Export_%s%s_%s.xlsx",
                departmentPart, statusPart, timestamp);
    }
}