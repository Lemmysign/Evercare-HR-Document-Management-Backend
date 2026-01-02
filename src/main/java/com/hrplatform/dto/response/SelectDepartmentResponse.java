package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectDepartmentResponse {

    private UUID staffId;
    private String staffIdNumber;
    private String fullName;
    private String email;
    private UUID departmentId;
    private String departmentName;
    private List<DocumentRequirementInfo> requiredDocuments;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentRequirementInfo {
        private UUID requirementId;
        private String documentName;
        private Boolean isRequired;
    }
}