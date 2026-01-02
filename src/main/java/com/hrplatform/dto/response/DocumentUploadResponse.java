package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {

    private UUID submissionId;
    private String documentName;
    private String cloudinaryUrl;
    private String fileName;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String message;
}