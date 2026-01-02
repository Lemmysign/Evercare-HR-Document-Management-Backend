package com.hrplatform.service.impl;

import com.hrplatform.dto.request.ConfigureDocumentRequirementsRequest;
import com.hrplatform.dto.response.DocumentRequirementResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentRequirement;
import com.hrplatform.exception.DuplicateResourceException;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.mapper.DocumentRequirementMapper;
import com.hrplatform.repository.DocumentRequirementRepository;
import com.hrplatform.service.AuditService;
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
    private final DocumentRequirementMapper documentRequirementMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public List<DocumentRequirementResponse> configureDocumentRequirements(
            UUID departmentId,
            ConfigureDocumentRequirementsRequest request,
            String hrUserEmail) {

        log.info("Configuring document requirements for department: {}", departmentId);

        Department department = departmentService.findById(departmentId);

        List<DocumentRequirement> existingRequirements =
                documentRequirementRepository.findByDepartmentId(departmentId);

        List<DocumentRequirement> updatedRequirements = new ArrayList<>();

        for (ConfigureDocumentRequirementsRequest.DocumentRequirementItem item : request.getDocumentRequirements()) {
            if (item.getId() != null) {
                // Update existing requirement
                DocumentRequirement existing = existingRequirements.stream()
                        .filter(req -> req.getId().equals(item.getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Document requirement not found with ID: " + item.getId()));

                String oldName = existing.getDocumentName();
                boolean oldRequired = existing.getIsRequired();

                existing.setDocumentName(item.getDocumentName());
                existing.setIsRequired(item.getIsRequired());

                updatedRequirements.add(existing);

                auditService.logDocumentConfigChange(
                        hrUserEmail,
                        department.getName(),
                        "EDIT",
                        item.getDocumentName(),
                        String.format("Updated: %s (Required: %b -> %b)", oldName, oldRequired, item.getIsRequired())
                );

            } else {
                // Add new requirement
                if (documentRequirementRepository.existsByDepartmentIdAndDocumentNameIgnoreCase(
                        departmentId, item.getDocumentName())) {
                    throw new DuplicateResourceException(
                            "Document requirement already exists: " + item.getDocumentName());
                }

                DocumentRequirement newRequirement = DocumentRequirement.builder()
                        .department(department)
                        .documentName(item.getDocumentName())
                        .isRequired(item.getIsRequired())
                        .isActive(true)
                        .build();

                updatedRequirements.add(newRequirement);

                auditService.logDocumentConfigChange(
                        hrUserEmail,
                        department.getName(),
                        "ADD",
                        item.getDocumentName(),
                        String.format("Added new requirement (Required: %b)", item.getIsRequired())
                );
            }
        }

        List<DocumentRequirement> savedRequirements =
                documentRequirementRepository.saveAll(updatedRequirements);

        log.info("Document requirements configured successfully for department: {}", departmentId);

        return savedRequirements.stream()
                .map(documentRequirementMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentRequirementResponse> getDocumentRequirementsByDepartment(UUID departmentId) {
        List<DocumentRequirement> requirements =
                documentRequirementRepository.findByDepartmentIdAndIsActiveTrue(departmentId);

        return requirements.stream()
                .map(documentRequirementMapper::toResponse)
                .collect(Collectors.toList());
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
        log.info("Deleting document requirement: {}", requirementId);

        DocumentRequirement requirement = findById(requirementId);

        // Soft delete by marking as inactive
        requirement.setIsActive(false);
        documentRequirementRepository.save(requirement);

        auditService.logDocumentConfigChange(
                hrUserEmail,
                requirement.getDepartment().getName(),
                "DELETE",
                requirement.getDocumentName(),
                "Document requirement deleted (soft delete)"
        );

        log.info("Document requirement deleted successfully: {}", requirementId);
    }
}