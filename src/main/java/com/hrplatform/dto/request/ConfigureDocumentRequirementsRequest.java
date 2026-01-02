package com.hrplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ConfigureDocumentRequirementsRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Document requirements list is required")
    private List<DocumentRequirementItem> documentRequirements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentRequirementItem {

        private UUID id; // Null for new documents

        @NotBlank(message = "Document name is required")
        private String documentName;

        @NotNull(message = "Required flag must be specified")
        private Boolean isRequired;
    }
}