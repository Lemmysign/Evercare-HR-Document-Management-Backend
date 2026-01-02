package com.hrplatform.controller;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.service.DocumentRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/configure/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Configure document requirements", description = "Configure document requirements for a department (HR only)")
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

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get document requirements by department", description = "Retrieve document requirements for a department (Public)")
    public ResponseEntity<ApiResponse<List<DocumentRequirementResponse>>> getDocumentRequirementsByDepartment(
            @PathVariable UUID departmentId) {

        log.info("Fetching document requirements for department: {}", departmentId);

        List<DocumentRequirementResponse> responses = documentRequirementService
                .getDocumentRequirementsByDepartment(departmentId);

        return ResponseEntity.ok(ApiResponse.success(responses,
                "Document requirements retrieved successfully"));
    }

    @DeleteMapping("/{requirementId}")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete document requirement", description = "Delete a document requirement (HR only)")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentRequirement(
            @PathVariable UUID requirementId,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Deleting document requirement: {} by HR user: {}", requirementId, hrUserEmail);

        documentRequirementService.deleteDocumentRequirement(requirementId, hrUserEmail);

        return ResponseEntity.ok(ApiResponse.success(null, "Document requirement deleted successfully"));
    }
}