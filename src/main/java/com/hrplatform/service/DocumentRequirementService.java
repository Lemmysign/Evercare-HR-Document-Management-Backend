package com.hrplatform.service;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.request.CreateDocumentRequirementRequest;
import com.hrplatform.dto.request.UpdateDocumentRequirementRequest;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.entity.DocumentRequirement;

import java.util.List;
import java.util.UUID;

public interface DocumentRequirementService {

    /**
     * Create a single document requirement
     */
    DocumentRequirementResponse createDocumentRequirement(
            CreateDocumentRequirementRequest request,
            String hrUserEmail);

    /**
     * Update a single document requirement
     */
    DocumentRequirementResponse updateDocumentRequirement(
            UUID requirementId,
            UpdateDocumentRequirementRequest request,
            String hrUserEmail);

    /**
     * Bulk configure document requirements for a department
     */
    List<DocumentRequirementResponse> configureDocumentRequirements(
            UUID departmentId,
            ConfigureDocumentRequirementsRequest request,
            String hrUserEmail);

    /**
     * Get all document requirements for a department (only active ones)
     */
    List<DocumentRequirementResponse> getDocumentRequirementsByDepartment(UUID departmentId);

    /**
     * Get all document requirements for a department (including inactive)
     */
    List<DocumentRequirementResponse> getAllDocumentRequirementsByDepartment(UUID departmentId);

    /**
     * Get a single document requirement by ID
     */
    DocumentRequirementResponse getDocumentRequirementById(UUID requirementId);

    /**
     * Find entity by ID (for internal use)
     */
    DocumentRequirement findById(UUID requirementId);

    /**
     * Delete a document requirement
     */
    void deleteDocumentRequirement(UUID requirementId, String hrUserEmail);
}