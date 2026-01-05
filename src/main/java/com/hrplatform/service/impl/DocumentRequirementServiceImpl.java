package com.hrplatform.service.impl;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.request.CreateDocumentRequirementRequest;
import com.hrplatform.dto.request.UpdateDocumentRequirementRequest;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentRequirement;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.exception.DuplicateResourceException;
import com.hrplatform.repository.DocumentRequirementRepository;
import com.hrplatform.service.DepartmentService;
import com.hrplatform.service.DocumentRequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRequirementServiceImpl implements DocumentRequirementService {

    private final DocumentRequirementRepository documentRequirementRepository;
    private final DepartmentService departmentService;

    @Override
    @Transactional
    public DocumentRequirementResponse createDocumentRequirement(
            CreateDocumentRequirementRequest request,
            String hrUserEmail) {

        log.info("HR user {} creating document requirement: {} for department: {}",
                hrUserEmail, request.getDocumentName(), request.getDepartmentId());

        // Validate department exists
        Department department = departmentService.findById(request.getDepartmentId());

        // Check for duplicate document name in the same department
        if (documentRequirementRepository.existsByDepartmentIdAndDocumentNameIgnoreCase(
                request.getDepartmentId(), request.getDocumentName())) {
            throw new DuplicateResourceException(
                    "Document requirement with name '" + request.getDocumentName() +
                            "' already exists for this department");
        }

        // Create document requirement
        DocumentRequirement documentRequirement = DocumentRequirement.builder()
                .department(department)
                .documentName(request.getDocumentName().trim())
                .isRequired(request.getIsRequired())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DocumentRequirement saved = documentRequirementRepository.save(documentRequirement);

        log.info("Document requirement created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DocumentRequirementResponse updateDocumentRequirement(
            UUID requirementId,
            UpdateDocumentRequirementRequest request,
            String hrUserEmail) {

        log.info("HR user {} updating document requirement: {}", hrUserEmail, requirementId);

        // Find existing requirement
        DocumentRequirement existing = findById(requirementId);

        // Check if document name is being changed and if it conflicts with another requirement
        if (!existing.getDocumentName().equalsIgnoreCase(request.getDocumentName())) {
            if (documentRequirementRepository.existsByDepartmentIdAndDocumentNameIgnoreCase(
                    existing.getDepartment().getId(), request.getDocumentName())) {
                throw new DuplicateResourceException(
                        "Document requirement with name '" + request.getDocumentName() +
                                "' already exists for this department");
            }
        }

        // Update fields
        existing.setDocumentName(request.getDocumentName().trim());
        existing.setIsRequired(request.getIsRequired());

        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        DocumentRequirement updated = documentRequirementRepository.save(existing);

        log.info("Document requirement updated successfully: {}", requirementId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public List<DocumentRequirementResponse> configureDocumentRequirements(
            UUID departmentId,
            ConfigureDocumentRequirementsRequest request,
            String hrUserEmail) {

        log.info("HR user {} configuring document requirements for department: {}",
                hrUserEmail, departmentId);

        // Validate department exists
        Department department = departmentService.findById(departmentId);

        // Validate request department ID matches path variable
        if (!departmentId.equals(request.getDepartmentId())) {
            throw new IllegalArgumentException("Department ID in path does not match request body");
        }

        List<DocumentRequirement> requirements = new ArrayList<>();

        for (ConfigureDocumentRequirementsRequest.DocumentRequirementItem item : request.getDocumentRequirements()) {
            DocumentRequirement requirement;

            if (item.getId() != null) {
                // Update existing requirement
                requirement = findById(item.getId());

                // Verify it belongs to the correct department
                if (!requirement.getDepartment().getId().equals(departmentId)) {
                    throw new IllegalArgumentException(
                            "Document requirement " + item.getId() + " does not belong to department " + departmentId);
                }

                // Check for duplicate name if name is being changed
                if (!requirement.getDocumentName().equalsIgnoreCase(item.getDocumentName())) {
                    if (documentRequirementRepository.existsByDepartmentIdAndDocumentNameIgnoreCase(
                            departmentId, item.getDocumentName())) {
                        throw new DuplicateResourceException(
                                "Document requirement with name '" + item.getDocumentName() +
                                        "' already exists for this department");
                    }
                }

                requirement.setDocumentName(item.getDocumentName().trim());
                requirement.setIsRequired(item.getIsRequired());
            } else {
                // Create new requirement
                // Check for duplicate
                if (documentRequirementRepository.existsByDepartmentIdAndDocumentNameIgnoreCase(
                        departmentId, item.getDocumentName())) {
                    throw new DuplicateResourceException(
                            "Document requirement with name '" + item.getDocumentName() +
                                    "' already exists for this department");
                }

                requirement = DocumentRequirement.builder()
                        .department(department)
                        .documentName(item.getDocumentName().trim())
                        .isRequired(item.getIsRequired())
                        .isActive(true)
                        .build();
            }

            requirements.add(requirement);
        }

        // Save all requirements
        List<DocumentRequirement> savedRequirements = documentRequirementRepository.saveAll(requirements);

        log.info("Configured {} document requirements for department: {}",
                savedRequirements.size(), departmentId);

        return savedRequirements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentRequirementResponse> getDocumentRequirementsByDepartment(UUID departmentId) {
        log.info("Fetching active document requirements for department: {}", departmentId);

        // Validate department exists
        departmentService.findById(departmentId);

        List<DocumentRequirement> requirements = documentRequirementRepository
                .findByDepartmentIdAndIsActiveTrue(departmentId);

        return requirements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentRequirementResponse> getAllDocumentRequirementsByDepartment(UUID departmentId) {
        log.info("Fetching all document requirements (including inactive) for department: {}", departmentId);

        // Validate department exists
        departmentService.findById(departmentId);

        List<DocumentRequirement> requirements = documentRequirementRepository
                .findByDepartmentId(departmentId);

        return requirements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentRequirementResponse getDocumentRequirementById(UUID requirementId) {
        log.info("Fetching document requirement by ID: {}", requirementId);

        DocumentRequirement requirement = findById(requirementId);
        return mapToResponse(requirement);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentRequirement findById(UUID requirementId) {
        return documentRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document requirement not found with ID: " + requirementId));
    }

    @Override
    @Transactional
    public void deleteDocumentRequirement(UUID requirementId, String hrUserEmail) {
        log.info("HR user {} deleting document requirement: {}", hrUserEmail, requirementId);

        DocumentRequirement requirement = findById(requirementId);

        // Check if already deleted (soft deleted)
        if (!requirement.getIsActive()) {
            throw new IllegalStateException(
                    "Document requirement is already deleted");
        }

        // Soft delete by setting isActive to false
        requirement.setIsActive(false);
        documentRequirementRepository.save(requirement);

        log.info("Document requirement deleted successfully: {}", requirementId);
    }

    private DocumentRequirementResponse mapToResponse(DocumentRequirement requirement) {
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