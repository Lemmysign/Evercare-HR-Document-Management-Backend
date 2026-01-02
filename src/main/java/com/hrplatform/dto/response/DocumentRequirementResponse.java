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
public class DocumentRequirementResponse {

    private UUID id;
    private String documentName;
    private Boolean isRequired;
    private Boolean isActive;
    private UUID departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
}