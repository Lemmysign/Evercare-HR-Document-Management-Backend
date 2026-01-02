package com.hrplatform.controller;

import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.DashboardMetricsResponse;
import com.hrplatform.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard metrics endpoints (HR Only)")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get dashboard metrics", description = "Retrieve dashboard metrics (HR only)")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboardMetrics() {

        log.info("Fetching dashboard metrics");

        DashboardMetricsResponse response = dashboardService.getDashboardMetrics();

        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard metrics retrieved successfully"));
    }
}