package com.hrplatform.controller;

import com.hrplatform.dto.request.CreateDepartmentRequest;
import com.hrplatform.dto.request.UpdateDepartmentRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.DepartmentResponse;
import com.hrplatform.dto.response.DepartmentStatsResponse;
import com.hrplatform.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department", description = "Department management endpoints (HR Only)")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Create department", description = "Create a new department (HR only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {

        log.info("Creating department: {}", request.getName());

        DepartmentResponse response = departmentService.createDepartment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Department created successfully"));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Update department", description = "Update an existing department (HR only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        log.info("Updating department: {}", departmentId);

        DepartmentResponse response = departmentService.updateDepartment(departmentId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Department updated successfully"));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get department by ID", description = "Retrieve department details (HR only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable UUID departmentId) {

        log.info("Fetching department: {}", departmentId);

        DepartmentResponse response = departmentService.getDepartmentById(departmentId);

        return ResponseEntity.ok(ApiResponse.success(response, "Department retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get all departments", description = "Retrieve all departments (HR only)")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {

        log.info("Fetching all departments");

        List<DepartmentResponse> responses = departmentService.getAllDepartments();

        return ResponseEntity.ok(ApiResponse.success(responses, "Departments retrieved successfully"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get department statistics", description = "Retrieve statistics for all departments (HR only)")
    public ResponseEntity<ApiResponse<List<DepartmentStatsResponse>>> getDepartmentStats() {

        log.info("Fetching department statistics");

        List<DepartmentStatsResponse> responses = departmentService.getAllDepartmentsWithStats();

        return ResponseEntity.ok(ApiResponse.success(responses, "Department statistics retrieved successfully"));
    }

    @GetMapping("/{departmentId}/stats")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get department statistics by ID", description = "Retrieve statistics for a specific department (HR only)")
    public ResponseEntity<ApiResponse<DepartmentStatsResponse>> getDepartmentStatsById(@PathVariable UUID departmentId) {

        log.info("Fetching statistics for department: {}", departmentId);

        DepartmentStatsResponse response = departmentService.getDepartmentStats(departmentId);

        return ResponseEntity.ok(ApiResponse.success(response, "Department statistics retrieved successfully"));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Delete department", description = "Delete a department (HR only)")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID departmentId) {

        log.info("Deleting department: {}", departmentId);

        departmentService.deleteDepartment(departmentId);

        return ResponseEntity.ok(ApiResponse.success(null, "Department deleted successfully"));
    }
}