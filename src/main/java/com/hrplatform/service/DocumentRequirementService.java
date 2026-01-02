package com.hrplatform.service;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.entity.DocumentRequirement;

import java.util.List;
import java.util.UUID;

public interface DocumentRequirementService {

    List<DocumentRequirementResponse> configureDocumentRequirements(
            UUID departmentId,
            ConfigureDocumentRequirementsRequest request,
            String hrUserEmail);

    List<DocumentRequirementResponse> getDocumentRequirementsByDepartment(UUID departmentId);

    DocumentRequirement findById(UUID requirementId);

    void deleteDocumentRequirement(UUID requirementId, String hrUserEmail);
}