package com.hrplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequirementRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotNull(message = "Required flag must be specified")
    private Boolean isRequired;

    @Builder.Default
    private Boolean isActive = true;
}