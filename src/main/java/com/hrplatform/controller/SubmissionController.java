package com.hrplatform.controller;

import com.hrplatform.dto.request.SubmissionFilterRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.PagedResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Submissions", description = "Submission management endpoints (HR Only)")
@SecurityRequirement(name = "Bearer Authentication")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/filter")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get filtered submissions", description = "Retrieve submissions with filters (HR only)")
    public ResponseEntity<ApiResponse<PagedResponse<SubmissionListResponse>>> getFilteredSubmissions(
            @RequestBody SubmissionFilterRequest request) {

        log.info("Fetching filtered submissions with criteria: {}", request);

        PagedResponse<SubmissionListResponse> response = submissionService.getFilteredSubmissions(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Submissions retrieved successfully"));
    }

    @GetMapping("/{staffId}/details")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get submission details", description = "Retrieve detailed submission information for a staff member (HR only)")
    public ResponseEntity<ApiResponse<SubmissionDetailsResponse>> getSubmissionDetails(
            @PathVariable UUID staffId) {

        log.info("Fetching submission details for staff: {}", staffId);

        SubmissionDetailsResponse response = submissionService.getSubmissionDetails(staffId);

        return ResponseEntity.ok(ApiResponse.success(response, "Submission details retrieved successfully"));
    }


    @GetMapping("/recent")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get recent submissions", description = "Retrieve last 5 staff submissions (HR only)")
    public ResponseEntity<ApiResponse<List<SubmissionListResponse>>> getRecentSubmissions() {
        log.info("Fetching recent submissions");

        List<SubmissionListResponse> response = submissionService.getRecentSubmissions();

        return ResponseEntity.ok(ApiResponse.success(response, "Recent submissions retrieved successfully"));
    }


}