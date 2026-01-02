package com.hrplatform.mapper;

import com.hrplatform.dto.response.DepartmentResponse;
import com.hrplatform.dto.response.DepartmentStatsResponse;
import com.hrplatform.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .build();
    }

    public DepartmentStatsResponse toStatsResponse(
            Department department,
            Long totalStaff,
            Long totalDocumentsSubmitted,
            Long staffWithSubmissions,
            Long staffWithoutSubmissions) {

        Double averageDocumentsPerStaff = totalStaff > 0
                ? (double) totalDocumentsSubmitted / totalStaff
                : 0.0;

        return DepartmentStatsResponse.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .totalStaff(totalStaff)
                .totalDocumentsSubmitted(totalDocumentsSubmitted)
                .staffWithSubmissions(staffWithSubmissions)
                .staffWithoutSubmissions(staffWithoutSubmissions)
                .averageDocumentsPerStaff(Math.round(averageDocumentsPerStaff * 100.0) / 100.0)
                .build();
    }
}