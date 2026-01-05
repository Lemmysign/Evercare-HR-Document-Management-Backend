package com.hrplatform.controller;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.request.CreateDocumentRequirementRequest;
import com.hrplatform.dto.request.UpdateDocumentRequirementRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.service.DocumentRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/document-requirements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Requirements", description = "Document requirement configuration endpoints")
public class DocumentRequirementController {

    private final DocumentRequirementService documentRequirementService;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create document requirement",
            description = "Create a single document requirement for a department (HR only)")
    public ResponseEntity<ApiResponse<DocumentRequirementResponse>> createDocumentRequirement(
            @Valid @RequestBody CreateDocumentRequirementRequest request,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Creating document requirement: {} for department: {} by HR user: {}",
                request.getDocumentName(), request.getDepartmentId(), hrUserEmail);

        DocumentRequirementResponse response = documentRequirementService
                .createDocumentRequirement(request, hrUserEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document requirement created successfully"));
    }

    // ==================== UPDATE ====================

    @PutMapping("/{requirementId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update document requirement",
            description = "Update a document requirement's name, required status, and active status (HR only)")
    public ResponseEntity<ApiResponse<DocumentRequirementResponse>> updateDocumentRequirement(
            @PathVariable UUID requirementId,
            @Valid @RequestBody UpdateDocumentRequirementRequest request,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Updating document requirement: {} by HR user: {}", requirementId, hrUserEmail);

        DocumentRequirementResponse response = documentRequirementService
                .updateDocumentRequirement(requirementId, request, hrUserEmail);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Document requirement updated successfully"));
    }

    // ==================== BULK CONFIGURE ====================

    @PostMapping("/configure/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Configure document requirements (Bulk)",
            description = "Bulk configure document requirements for a department - create and update multiple at once (HR only)")
    public ResponseEntity<ApiResponse<List<DocumentRequirementResponse>>> configureDocumentRequirements(
            @PathVariable UUID departmentId,
            @Valid @RequestBody ConfigureDocumentRequirementsRequest request,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Configuring document requirements for department: {} by HR user: {}",
                departmentId, hrUserEmail);

        List<DocumentRequirementResponse> responses = documentRequirementService
                .configureDocumentRequirements(departmentId, request, hrUserEmail);

        return ResponseEntity.ok(ApiResponse.success(responses,
                "Document requirements configured successfully"));
    }

    // ==================== READ ====================

    @GetMapping("/{requirementId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get document requirement by ID",
            description = "Retrieve a single document requirement by its ID")
    public ResponseEntity<ApiResponse<DocumentRequirementResponse>> getDocumentRequirementById(
            @PathVariable UUID requirementId) {

        log.info("Fetching document requirement: {}", requirementId);

        DocumentRequirementResponse response = documentRequirementService
                .getDocumentRequirementById(requirementId);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Document requirement retrieved successfully"));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get active document requirements by department",
            description = "Retrieve all active document requirements for a department (Public - for staff to see what they need to submit)")
    public ResponseEntity<ApiResponse<List<DocumentRequirementResponse>>> getDocumentRequirementsByDepartment(
            @PathVariable UUID departmentId) {

        log.info("Fetching active document requirements for department: {}", departmentId);

        List<DocumentRequirementResponse> responses = documentRequirementService
                .getDocumentRequirementsByDepartment(departmentId);

        return ResponseEntity.ok(ApiResponse.success(responses,
                "Document requirements retrieved successfully"));
    }

    @GetMapping("/department/{departmentId}/all")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all document requirements by department",
            description = "Retrieve all document requirements (including inactive) for a department (HR only)")
    public ResponseEntity<ApiResponse<List<DocumentRequirementResponse>>> getAllDocumentRequirementsByDepartment(
            @PathVariable UUID departmentId) {

        log.info("Fetching all document requirements (including inactive) for department: {}", departmentId);

        List<DocumentRequirementResponse> responses = documentRequirementService
                .getAllDocumentRequirementsByDepartment(departmentId);

        return ResponseEntity.ok(ApiResponse.success(responses,
                "All document requirements retrieved successfully"));
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{requirementId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete document requirement",
            description = "Soft delete a document requirement (sets isActive to false) (HR only)")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentRequirement(
            @PathVariable UUID requirementId,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Deleting document requirement: {} by HR user: {}", requirementId, hrUserEmail);

        documentRequirementService.deleteDocumentRequirement(requirementId, hrUserEmail);

        return ResponseEntity.ok(ApiResponse.success(null,
                "Document requirement deleted successfully"));
    }
}