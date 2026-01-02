package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsResponse {

    private UUID departmentId;
    private String departmentName;
    private Long totalStaff;
    private Long totalDocumentsSubmitted;
    private Long staffWithSubmissions;
    private Long staffWithoutSubmissions;
    private Double averageDocumentsPerStaff;
}