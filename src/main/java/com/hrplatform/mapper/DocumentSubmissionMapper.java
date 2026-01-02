package com.hrplatform.mapper;

import com.hrplatform.dto.response.DocumentUploadResponse;
import com.hrplatform.entity.DocumentSubmission;
import org.springframework.stereotype.Component;

@Component
public class DocumentSubmissionMapper {

    public DocumentUploadResponse toUploadResponse(DocumentSubmission submission, String message) {
        return DocumentUploadResponse.builder()
                .submissionId(submission.getId())
                .documentName(submission.getDocumentRequirement().getDocumentName())
                .cloudinaryUrl(submission.getCloudinaryUrl())
                .fileName(submission.getFileName())
                .fileSize(submission.getFileSize())
                .uploadedAt(submission.getCreatedAt())
                .message(message)
                .build();
    }
}