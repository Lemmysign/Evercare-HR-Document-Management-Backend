package com.hrplatform.controller;

import com.hrplatform.dto.request.DocumentUploadRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.DocumentUploadResponse;
import com.hrplatform.entity.Staff;
import com.hrplatform.service.DocumentSubmissionService;
import com.hrplatform.service.SessionService;
import com.hrplatform.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Submission", description = "Document submission endpoints (Public)")
public class DocumentSubmissionController {

    private final DocumentSubmissionService documentSubmissionService;
    private final SessionService sessionService;
    private final StaffService staffService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a single document for a staff member (staffIdNumber is passed as path parameter)")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @RequestParam("staffIdNumber") String staffIdNumber,
            @RequestParam("documentRequirementId") UUID documentRequirementId,
            @RequestParam("file") MultipartFile file) {

        log.info("Document upload request for staff: {}", staffIdNumber);

        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .documentRequirementId(documentRequirementId)
                .file(file)
                .build();

        DocumentUploadResponse response = documentSubmissionService.uploadDocument(staffIdNumber, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded successfully"));
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple documents using session token")
    public ResponseEntity<ApiResponse<List<DocumentUploadResponse>>> uploadMultipleDocuments(
            @RequestHeader("X-Session-Token") String sessionToken,
            @RequestParam("requirementIds") List<UUID> requirementIds,
            @RequestParam("files") List<MultipartFile> files) {

        log.info("Multiple document upload with session token ({} files)", files.size());

        // Validate session and get staffId
        Map<String, String> session = sessionService.validateSession(sessionToken);
        UUID staffId = UUID.fromString(session.get("staffId"));

        // Get staff by ID
        Staff staff = staffService.findById(staffId);

        List<DocumentUploadResponse> responses = documentSubmissionService
                .uploadMultipleDocumentsWithStaff(staff, requirementIds, files);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses, "Documents uploaded successfully"));
    }
}