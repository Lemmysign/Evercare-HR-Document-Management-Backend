package com.hrplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Document requirement ID is required")
    private UUID documentRequirementId;

    @NotNull(message = "File is required")
    private MultipartFile file;
}