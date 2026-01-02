package com.hrplatform.service;

import com.hrplatform.dto.request.CreateDepartmentRequest;
import com.hrplatform.dto.request.UpdateDepartmentRequest;
import com.hrplatform.dto.response.DepartmentResponse;
import com.hrplatform.dto.response.DepartmentStatsResponse;
import com.hrplatform.entity.Department;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(UUID departmentId, UpdateDepartmentRequest request);

    DepartmentResponse getDepartmentById(UUID departmentId);

    List<DepartmentResponse> getAllDepartments();

    void deleteDepartment(UUID departmentId);

    Department findById(UUID departmentId);

    Long countTotalDepartments();

    List<DepartmentStatsResponse> getAllDepartmentsWithStats();

    DepartmentStatsResponse getDepartmentStats(UUID departmentId);
}