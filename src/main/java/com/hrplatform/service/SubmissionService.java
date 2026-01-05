package com.hrplatform.service;

import com.hrplatform.dto.request.SubmissionFilterRequest;
import com.hrplatform.dto.response.PagedResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {

    PagedResponse<SubmissionListResponse> getFilteredSubmissions(SubmissionFilterRequest request);

    SubmissionDetailsResponse getSubmissionDetails(UUID staffId);

    List<SubmissionListResponse> getRecentSubmissions();

    // NEW: Get all staff with pagination
    PagedResponse<SubmissionListResponse> getAllStaff(int page, int size);

    PagedResponse<SubmissionListResponse> getAllStaffOrderedBySubmissions(int page, int size);


    PagedResponse<SubmissionListResponse> getAllStaffFiltered(
            int page,
            int size,
            String search,
            UUID departmentId,
            String status
    );
}