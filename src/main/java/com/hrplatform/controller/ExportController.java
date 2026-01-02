package com.hrplatform.controller;

import com.hrplatform.dto.request.ExportFilterRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.ExportResponse;
import com.hrplatform.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Data export endpoints (HR Only)")
@SecurityRequirement(name = "Bearer Authentication")
public class ExportController {


    private final ExportService exportService;

    @PostMapping("/excel")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestBody ExportFilterRequest request,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Export request by HR user: {} with filters: {}", hrUserEmail, request);

        ExportResponse response = exportService.exportToExcel(request, hrUserEmail);

        // Force .xlsx extension even if service forgot to add it
        String fileName = response.getFileName().endsWith(".xlsx")
                ? response.getFileName()
                : response.getFileName() + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getContentType()));

        // Use normalized filename
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"");

        headers.setContentLength(response.getFileData().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getFileData());
    }


    @PostMapping("/excel/metadata")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get export metadata", description = "Get export metadata without downloading (HR only)")
    public ResponseEntity<ApiResponse<ExportResponse>> getExportMetadata(
            @RequestBody ExportFilterRequest request,
            Authentication authentication) {

        String hrUserEmail = authentication.getName();
        log.info("Export metadata request by HR user: {}", hrUserEmail);

        ExportResponse response = exportService.exportToExcel(request, hrUserEmail);

        // Return metadata without file data
        ExportResponse metadata = ExportResponse.builder()
                .fileName(response.getFileName())
                .contentType(response.getContentType())
                .totalRecords(response.getTotalRecords())
                .message(response.getMessage())
                .build();

        return ResponseEntity.ok(ApiResponse.success(metadata, "Export metadata retrieved successfully"));
    }
}