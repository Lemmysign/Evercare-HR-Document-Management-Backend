package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {

    private Long totalDepartments;
    private Long totalStaff;
    private Long totalDocumentSubmissions;
    private Long staffWithSubmissions;
    private Long staffWithoutSubmissions;
}