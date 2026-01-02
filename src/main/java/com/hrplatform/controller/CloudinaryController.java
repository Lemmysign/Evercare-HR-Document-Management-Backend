package com.hrplatform.controller;

import com.hrplatform.dto.request.GenerateSignatureRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.CloudinarySignatureResponse;
import com.hrplatform.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cloudinary", description = "Cloudinary signature generation endpoints (Public)")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/signature")
    @Operation(summary = "Generate upload signature", description = "Generate a signed upload signature for Cloudinary")
    public ResponseEntity<ApiResponse<CloudinarySignatureResponse>> generateUploadSignature(
            @Valid @RequestBody GenerateSignatureRequest request) {

        log.info("Generating Cloudinary signature for staff: {}", request.getStaffIdNumber());

        CloudinarySignatureResponse response = cloudinaryService.generateUploadSignature(
                request.getStaffIdNumber(),
                request.getDocumentRequirementId(),
                request.getFileName()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Signature generated successfully"));
    }
}