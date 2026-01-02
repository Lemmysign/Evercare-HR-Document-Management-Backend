package com.hrplatform.service.impl;

import com.hrplatform.dto.request.SelectDepartmentRequest;
import com.hrplatform.dto.request.StaffValidationRequest;
import com.hrplatform.dto.response.SelectDepartmentResponse;
import com.hrplatform.dto.response.StaffRequirementsResponse;
import com.hrplatform.dto.response.StaffValidationResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentRequirement;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import com.hrplatform.exception.BadRequestException;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.exception.StaffValidationException;
import com.hrplatform.mapper.StaffMapper;
import com.hrplatform.repository.DepartmentRepository;
import com.hrplatform.repository.DocumentRequirementRepository;
import com.hrplatform.repository.DocumentSubmissionRepository;
import com.hrplatform.repository.StaffRepository;
import com.hrplatform.service.AuditService;
import com.hrplatform.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;
    private final DocumentRequirementRepository documentRequirementRepository;
    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final StaffMapper staffMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public StaffValidationResponse validateStaff(StaffValidationRequest request) {
        log.info("Validating staff with ID: {} and email: {}", request.getStaffIdNumber(), request.getEmail());

        // Case-insensitive search
        Staff staff = staffRepository.findByStaffIdNumberAndEmailIgnoreCase(
                request.getStaffIdNumber().trim(),
                request.getEmail().trim()
        ).orElseThrow(() -> {
            log.warn("Staff validation failed for ID: {} and email: {}",
                    request.getStaffIdNumber(), request.getEmail());

            auditService.logStaffValidation(
                    request.getStaffIdNumber(),
                    "UNKNOWN",
                    "Validation failed - Invalid credentials"
            );

            return new StaffValidationException("Invalid Staff ID or Email. Please check your credentials.");
        });

        StaffValidationResponse response = staffMapper.toValidationResponse(staff, true, "Validation successful");

        // Check if staff has a department assigned
        if (staff.getDepartment() == null) {
            // Staff has NO department - return available departments
            log.info("Staff has no department assigned. Returning available departments.");

            List<Department> departments = departmentRepository.findAll();
            List<StaffValidationResponse.DepartmentOption> departmentOptions = departments.stream()
                    .map(dept -> StaffValidationResponse.DepartmentOption.builder()
                            .departmentId(dept.getId())
                            .departmentName(dept.getName())
                            .description(dept.getDescription())
                            .build())
                    .collect(Collectors.toList());

            response.setHasDepartment(false);
            response.setAvailableDepartments(departmentOptions);
            response.setMessage("Validation successful. Please select your department.");
        } else {
            // Staff already HAS a department
            log.info("Staff already assigned to department: {}", staff.getDepartment().getName());
            response.setHasDepartment(true);
            response.setAvailableDepartments(null);
            response.setMessage("Validation successful.");
        }

        auditService.logStaffValidation(
                staff.getStaffIdNumber(),
                staff.getDepartment() != null ? staff.getDepartment().getName() : "NOT_SET",
                "Validation successful"
        );

        log.info("Staff validation successful for: {}", staff.getStaffIdNumber());

        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public StaffRequirementsResponse getStaffRequirements(UUID staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        if (staff.getDepartment() == null) {
            throw new BadRequestException("Staff has no department assigned");
        }

        List<DocumentRequirement> requirements = documentRequirementRepository
                .findByDepartmentIdAndIsActiveTrue(staff.getDepartment().getId());

        List<StaffRequirementsResponse.DocumentRequirementInfo> docInfos = requirements.stream()
                .map(req -> StaffRequirementsResponse.DocumentRequirementInfo.builder()
                        .requirementId(req.getId())
                        .documentName(req.getDocumentName())
                        .isRequired(req.getIsRequired())
                        .build())
                .collect(Collectors.toList());

        return StaffRequirementsResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .departmentName(staff.getDepartment().getName())
                .requiredDocuments(docInfos)
                .build();
    }



    @Override
    @Transactional
    public SelectDepartmentResponse selectDepartment(SelectDepartmentRequest request) {
        log.info("Staff {} selecting department: {}", request.getStaffIdNumber(), request.getDepartmentId());

        // Find staff (case-insensitive)
        Staff staff = staffRepository.findByStaffIdNumberIgnoreCase(request.getStaffIdNumber().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + request.getStaffIdNumber()));


        // Find department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        // Assign department to staff
        staff.setDepartment(department);
        staffRepository.save(staff);

        // Get document requirements for the selected department
        List<DocumentRequirement> requirements = documentRequirementRepository
                .findByDepartmentIdAndIsActiveTrue(department.getId());

        List<SelectDepartmentResponse.DocumentRequirementInfo> documentInfos = requirements.stream()
                .map(req -> SelectDepartmentResponse.DocumentRequirementInfo.builder()
                        .requirementId(req.getId())
                        .documentName(req.getDocumentName())
                        .isRequired(req.getIsRequired())
                        .build())
                .collect(Collectors.toList());

        auditService.logStaffValidation(
                staff.getStaffIdNumber(),
                department.getName(),
                "Department selected: " + department.getName()
        );

        log.info("Department selected successfully for staff: {}", staff.getStaffIdNumber());

        return SelectDepartmentResponse.builder()
                .staffId(staff.getId())
                .staffIdNumber(staff.getStaffIdNumber())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .departmentId(department.getId())
                .departmentName(department.getName())
                .requiredDocuments(documentInfos)
                .message("Department selected successfully. Please upload the required documents.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Staff findByStaffIdNumber(String staffIdNumber) {
        return staffRepository.findByStaffIdNumberIgnoreCase(staffIdNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffIdNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Staff findById(UUID id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDetailsResponse getStaffSubmissionDetails(UUID staffId) {
        Staff staff = staffRepository.findByIdWithDocuments(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        List<DocumentSubmission> submissions = documentSubmissionRepository.findByStaffIdWithDetails(staffId);

        return staffMapper.toSubmissionDetailsResponse(staff, submissions);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countTotalStaff() {
        return staffRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countStaffWithSubmissions() {
        return staffRepository.countStaffWithSubmissions();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countStaffWithoutSubmissions() {
        return staffRepository.countStaffWithoutSubmissions();
    }
}