package com.hrplatform.service;

import com.hrplatform.dto.request.SelectDepartmentRequest;
import com.hrplatform.dto.request.StaffValidationRequest;
import com.hrplatform.dto.response.SelectDepartmentResponse;
import com.hrplatform.dto.response.StaffRequirementsResponse;
import com.hrplatform.dto.response.StaffValidationResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.entity.Staff;

import java.util.UUID;

public interface StaffService {

    StaffValidationResponse validateStaff(StaffValidationRequest request);

    SelectDepartmentResponse selectDepartment(SelectDepartmentRequest request);

    Staff findByStaffIdNumber(String staffIdNumber);

    Staff findById(UUID id);

    SubmissionDetailsResponse getStaffSubmissionDetails(UUID staffId);

    Long countTotalStaff();

    Long countStaffWithSubmissions();

    Long countStaffWithoutSubmissions();

    StaffRequirementsResponse getStaffRequirements(UUID staffId);
}