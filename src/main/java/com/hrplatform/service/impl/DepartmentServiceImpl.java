package com.hrplatform.service.impl;

import com.hrplatform.dto.request.CreateDepartmentRequest;
import com.hrplatform.dto.request.UpdateDepartmentRequest;
import com.hrplatform.dto.response.DepartmentResponse;
import com.hrplatform.dto.response.DepartmentStatsResponse;
import com.hrplatform.entity.Department;
import com.hrplatform.exception.DuplicateResourceException;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.mapper.DepartmentMapper;
import com.hrplatform.repository.DepartmentRepository;
import com.hrplatform.repository.DocumentSubmissionRepository;
import com.hrplatform.repository.StaffRepository;
import com.hrplatform.service.DepartmentService;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;
    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department: {}", request.getName());

        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department already exists with name: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department savedDepartment = departmentRepository.save(department);

        log.info("Department created successfully with ID: {}", savedDepartment.getId());

        return departmentMapper.toResponse(savedDepartment);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(UUID departmentId, UpdateDepartmentRequest request) {
        log.info("Updating department with ID: {}", departmentId);

        Department department = findById(departmentId);

        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department already exists with name: " + request.getName());
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updatedDepartment = departmentRepository.save(department);

        log.info("Department updated successfully: {}", updatedDepartment.getId());

        return departmentMapper.toResponse(updatedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID departmentId) {
        Department department = findById(departmentId);
        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID departmentId) {
        log.info("Deleting department with ID: {}", departmentId);

        Department department = findById(departmentId);

        Long staffCount = staffRepository.countByDepartmentId(departmentId);
        if (staffCount > 0) {
            throw new IllegalStateException("Cannot delete department with existing staff members");
        }

        departmentRepository.delete(department);

        log.info("Department deleted successfully: {}", departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Department findById(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Long countTotalDepartments() {
        return departmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentStatsResponse> getAllDepartmentsWithStats() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(this::buildDepartmentStats)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentStatsResponse getDepartmentStats(UUID departmentId) {
        Department department = findById(departmentId);
        return buildDepartmentStats(department);
    }

    private DepartmentStatsResponse buildDepartmentStats(Department department) {
        Long totalStaff = staffRepository.countByDepartmentId(department.getId());
        Long totalDocuments = departmentRepository.countDocumentSubmissionsByDepartment(department.getId());
        Long staffWithSubmissions = departmentRepository.countStaffWithSubmissionsByDepartment(department.getId());
        Long staffWithoutSubmissions = totalStaff - staffWithSubmissions;

        return departmentMapper.toStatsResponse(
                department,
                totalStaff,
                totalDocuments,
                staffWithSubmissions,
                staffWithoutSubmissions
        );
    }
}