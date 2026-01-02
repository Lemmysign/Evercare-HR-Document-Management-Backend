package com.hrplatform.controller;

import com.hrplatform.dto.request.SelectDepartmentRequest;
import com.hrplatform.dto.request.StaffValidationRequest;
import com.hrplatform.dto.response.*;
import com.hrplatform.service.SessionService;
import com.hrplatform.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Staff", description = "Staff validation and management endpoints (Public)")
public class StaffController {

    private final StaffService staffService;
    private final SessionService sessionService; // ADD THIS


    @PostMapping("/validate")
    @Operation(summary = "Validate staff credentials",
            description = "Validates staff ID and email. Returns available departments if not yet selected.")
    public ResponseEntity<ApiResponse<StaffValidationResponse>> validateStaff(
            @Valid @RequestBody StaffValidationRequest request) {

        log.info("Staff validation request received for: {}", request.getStaffIdNumber());

        StaffValidationResponse response = staffService.validateStaff(request);

        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/select-department")
    @Operation(summary = "Select department and create session",
            description = "Staff selects their department and receives a session token")
    public ResponseEntity<ApiResponse<SessionResponse>> selectDepartment(
            @Valid @RequestBody SelectDepartmentRequest request) {

        log.info("Department selection request for staff: {}", request.getStaffIdNumber());

        SelectDepartmentResponse response = staffService.selectDepartment(request);

        // Create session token
        String sessionToken = sessionService.createSession(
                response.getStaffId(),
                response.getDepartmentId()
        );

        SessionResponse sessionResponse = SessionResponse.builder()
                .sessionToken(sessionToken)
                .expiresIn(3600L)
                .staffName(response.getFullName())
                .departmentName(response.getDepartmentName())
                .build();

        return ResponseEntity.ok(ApiResponse.success(sessionResponse,
                "Department selected successfully"));
    }

    @GetMapping("/requirements")
    @Operation(summary = "Get staff requirements using session token")
    public ResponseEntity<ApiResponse<StaffRequirementsResponse>> getStaffRequirements(
            @RequestHeader("X-Session-Token") String sessionToken) {

        log.info("Fetching requirements with session token");

        Map<String, String> session = sessionService.validateSession(sessionToken);
        UUID staffId = UUID.fromString(session.get("staffId"));

        StaffRequirementsResponse response = staffService.getStaffRequirements(staffId);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Requirements fetched successfully"));
    }

    @DeleteMapping("/clearstaffsessions")
    @Operation(summary = "invalidate session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-Session-Token") String sessionToken) {

        log.info("Logout request received");

        try {
            sessionService.invalidateSession(sessionToken);
            return ResponseEntity.ok(ApiResponse.success(null, "Session invalidated successfully"));
        } catch (Exception e) {
            log.warn("Failed to invalidate session: {}", e.getMessage());
            // Return success anyway - user is logging out
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out"));
        }
    }


}