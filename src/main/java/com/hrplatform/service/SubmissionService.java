package com.hrplatform.service;

import com.hrplatform.dto.request.SubmissionFilterRequest;
import com.hrplatform.dto.response.PagedResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;

import java.util.UUID;

public interface SubmissionService {

    PagedResponse<SubmissionListResponse> getFilteredSubmissions(SubmissionFilterRequest request);

    SubmissionDetailsResponse getSubmissionDetails(UUID staffId);
}