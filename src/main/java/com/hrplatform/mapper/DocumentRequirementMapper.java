package com.hrplatform.mapper;

import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.entity.DocumentRequirement;
import org.springframework.stereotype.Component;

@Component
public class DocumentRequirementMapper {

    public DocumentRequirementResponse toResponse(DocumentRequirement requirement) {
        return DocumentRequirementResponse.builder()
                .id(requirement.getId())
                .documentName(requirement.getDocumentName())
                .isRequired(requirement.getIsRequired())
                .isActive(requirement.getIsActive())
                .departmentId(requirement.getDepartment().getId())
                .departmentName(requirement.getDepartment().getName())
                .createdAt(requirement.getCreatedAt())
                .build();
    }
}