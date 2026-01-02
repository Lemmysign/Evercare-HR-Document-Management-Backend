package com.hrplatform.service.impl;

import com.hrplatform.dto.request.ExportFilterRequest;
import com.hrplatform.dto.response.ExportResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
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
        log.info("Exporting data to Excel for user: {} with filters: {}", hrUserEmail, request);

        // Fetch staff based on filters
        List<Staff> staffList = fetchStaffByFilters(request);

        // Fetch all submissions for these staff
        List<DocumentSubmission> allSubmissions = fetchSubmissionsByFilters(request, staffList);

        // Generate Excel file
        byte[] excelData = excelExportService.generateExcelFile(staffList, allSubmissions);

        // Generate filename
        String fileName = generateFileName(request);

        // Log export
        String departmentName = request.getDepartmentId() != null
                ? departmentService.findById(request.getDepartmentId()).getName()
                : "All Departments";

        exportLogRepository.save(
                exportLogMapper.toEntity(hrUserEmail, request, staffList.size(), departmentName)
        );

        auditService.logHrActivity(
                hrUserEmail,
                "EXPORT",
                departmentName,
                String.format("Exported %d staff records", staffList.size())
        );

        log.info("Export completed successfully. Total records: {}", staffList.size());

        return ExportResponse.builder()
                .fileName(fileName)
                .fileData(excelData)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .totalRecords(staffList.size())
                .message("Export completed successfully")
                .build();
    }

    private List<Staff> fetchStaffByFilters(ExportFilterRequest request) {
        List<Staff> staffList;

        if (request.getDepartmentId() != null) {
            Department department = departmentService.findById(request.getDepartmentId());
            staffList = staffRepository.findAll().stream()
                    .filter(staff -> staff.getDepartment().getId().equals(request.getDepartmentId()))
                    .collect(Collectors.toList());
        } else {
            staffList = staffRepository.findAll();
        }

        // Apply submission status filter
        if ("SUBMITTED".equalsIgnoreCase(request.getSubmissionStatus())) {
            staffList = staffList.stream()
                    .filter(staff -> documentSubmissionRepository.countByStaffId(staff.getId()) > 0)
                    .collect(Collectors.toList());
        } else if ("UNSUBMITTED".equalsIgnoreCase(request.getSubmissionStatus())) {
            staffList = staffList.stream()
                    .filter(staff -> documentSubmissionRepository.countByStaffId(staff.getId()) == 0)
                    .collect(Collectors.toList());
        }

        return staffList;
    }

    private List<DocumentSubmission> fetchSubmissionsByFilters(ExportFilterRequest request, List<Staff> staffList) {
        List<DocumentSubmission> submissions;

        if (request.getDateRangeStart() != null && request.getDateRangeEnd() != null) {
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

        return submissions.stream()
                .filter(submission -> staffIds.contains(submission.getStaff().getId()))
                .collect(Collectors.toList());
    }

    private String generateFileName(ExportFilterRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        String departmentPart = request.getDepartmentId() != null
                ? departmentService.findById(request.getDepartmentId()).getName().replaceAll("\\s+", "_")
                : "AllDepartments";

        return String.format("Staff_Documents_Export_%s_%s.xlsx", departmentPart, timestamp);
    }
}