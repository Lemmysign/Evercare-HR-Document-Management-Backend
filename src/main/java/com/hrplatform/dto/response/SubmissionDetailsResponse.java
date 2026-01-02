package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDetailsResponse {

    private UUID staffId;
    private String staffIdNumber;
    private String fullName;
    private String email;
    private String departmentName;
    private List<DocumentSubmissionInfo> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentSubmissionInfo {
        private UUID submissionId;
        private String documentName;
        private String cloudinaryUrl;
        private String fileName;
        private Long fileSize;
        private LocalDateTime uploadedAt;
    }
}