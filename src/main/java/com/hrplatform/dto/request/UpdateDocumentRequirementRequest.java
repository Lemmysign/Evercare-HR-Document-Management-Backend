package com.hrplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentRequirementRequest {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotNull(message = "Required flag must be specified")
    private Boolean isRequired;

    private Boolean isActive;
}