package com.hrplatform.service.impl;

import com.hrplatform.dto.response.DashboardMetricsResponse;
import com.hrplatform.service.DashboardService;
import com.hrplatform.service.DepartmentService;
import com.hrplatform.service.DocumentSubmissionService;
import com.hrplatform.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final DepartmentService departmentService;
    private final StaffService staffService;
    private final DocumentSubmissionService documentSubmissionService;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        log.info("Fetching dashboard metrics");

        Long totalDepartments = departmentService.countTotalDepartments();
        Long totalStaff = staffService.countTotalStaff();
        Long totalSubmissions = documentSubmissionService.countTotalSubmissions();
        Long staffWithSubmissions = staffService.countStaffWithSubmissions();
        Long staffWithoutSubmissions = staffService.countStaffWithoutSubmissions();

        return DashboardMetricsResponse.builder()
                .totalDepartments(totalDepartments)
                .totalStaff(totalStaff)
                .totalDocumentSubmissions(totalSubmissions)
                .staffWithSubmissions(staffWithSubmissions)
                .staffWithoutSubmissions(staffWithoutSubmissions)
                .build();
    }
}