package com.hrplatform.service.impl;

import com.hrplatform.dto.request.SubmissionFilterRequest;
import com.hrplatform.dto.response.PagedResponse;
import com.hrplatform.dto.response.SubmissionDetailsResponse;
import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.entity.Staff;
import com.hrplatform.exception.BadRequestException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hrplatform.repository.StaffRepository;
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


    @Override
    @Transactional(readOnly = true)
    public List<SubmissionListResponse> getRecentSubmissions() {
        log.info("Fetching last 5 submissions");

        Pageable pageable = PageRequest.of(0, 5);
        Page<Staff> recentStaffPage = staffRepository.findStaffWithRecentSubmissions(pageable);

        return recentStaffPage.getContent().stream()
                .map(staff -> {
                    Long submissionCount = documentSubmissionRepository.countByStaffId(staff.getId());
                    return staffMapper.toSubmissionListResponse(staff, submissionCount);
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubmissionListResponse> getAllStaff(int page, int size) {
        log.info("Fetching all staff - Page: {}, Size: {}", page, size);

        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<SubmissionListResponse> staffPage = staffRepository.findAllStaffWithSubmissionCount(pageable);

        log.info("Retrieved {} staff records out of {} total",
                staffPage.getNumberOfElements(),
                staffPage.getTotalElements());

        return PagedResponse.<SubmissionListResponse>builder()
                .content(staffPage.getContent())
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
    public PagedResponse<SubmissionListResponse> getAllStaffOrderedBySubmissions(int page, int size) {
        log.info("Fetching all staff ordered by submissions - Page: {}, Size: {}", page, size);

        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<SubmissionListResponse> staffPage = staffRepository.findAllStaffOrderedBySubmissions(pageable);

        log.info("Retrieved {} staff records ordered by submissions",
                staffPage.getNumberOfElements());

        return PagedResponse.<SubmissionListResponse>builder()
                .content(staffPage.getContent())
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
    public PagedResponse<SubmissionListResponse> getAllStaffFiltered(
            int page,
            int size,
            String search,
            UUID departmentId,
            String status) {

        log.info("Fetching filtered staff - Page: {}, Size: {}, Search: {}, Dept: {}, Status: {}",
                page, size, search, departmentId, status);

        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<SubmissionListResponse> staffPage = staffRepository.findAllStaffWithFilters(
                search, departmentId, status, pageable);

        log.info("Retrieved {} staff records out of {} total",
                staffPage.getNumberOfElements(),
                staffPage.getTotalElements());

        return PagedResponse.<SubmissionListResponse>builder()
                .content(staffPage.getContent())
                .pageNumber(staffPage.getNumber())
                .pageSize(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .last(staffPage.isLast())
                .first(staffPage.isFirst())
                .build();
    }


}