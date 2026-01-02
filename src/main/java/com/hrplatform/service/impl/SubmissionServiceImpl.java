package com.hrplatform.service.impl;

import com.hrplatform.dto.request.SubmissionFilterRequest;
import com.hrplatform.dto.response.PagedResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.entity.Staff;
import com.hrplatform.mapper.StaffMapper;
import com.hrplatform.repository.DocumentSubmissionRepository;
import com.hrplatform.repository.StaffRepository;
import com.hrplatform.service.StaffService;
import com.hrplatform.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final StaffRepository staffRepository;
    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final StaffService staffService;
    private final StaffMapper staffMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubmissionListResponse> getFilteredSubmissions(SubmissionFilterRequest request) {
        log.info("Fetching filtered submissions with criteria: {}", request);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Staff> staffPage;

        // Apply filters
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            if (request.getDepartmentId() != null) {
                staffPage = staffRepository.searchStaffByDepartment(
                        request.getDepartmentId(),
                        request.getSearchTerm().trim(),
                        pageable
                );
            } else {
                staffPage = staffRepository.searchStaff(request.getSearchTerm().trim(), pageable);
            }
        } else if (request.getDepartmentId() != null) {
            staffPage = staffRepository.findByDepartmentId(request.getDepartmentId(), pageable);
        } else {
            staffPage = staffRepository.findAll(pageable);
        }

        List<SubmissionListResponse> content = staffPage.getContent().stream()
                .map(staff -> {
                    Long submissionCount = documentSubmissionRepository.countByStaffId(staff.getId());

                    // Apply submission status filter
                    if ("SUBMITTED".equalsIgnoreCase(request.getSubmissionStatus()) && submissionCount == 0) {
                        return null;
                    }
                    if ("UNSUBMITTED".equalsIgnoreCase(request.getSubmissionStatus()) && submissionCount > 0) {
                        return null;
                    }

                    return staffMapper.toSubmissionListResponse(staff, submissionCount);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());

        return PagedResponse.<SubmissionListResponse>builder()
                .content(content)
                .pageNumber(staffPage.getNumber())
                .pageSize(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .last(staffPage.isLast())
                .first(staffPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDetailsResponse getSubmissionDetails(UUID staffId) {
        return staffService.getStaffSubmissionDetails(staffId);
    }
}