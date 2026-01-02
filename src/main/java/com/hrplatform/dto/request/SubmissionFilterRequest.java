package com.hrplatform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFilterRequest {

    private UUID departmentId; // null = all departments

    private String submissionStatus; // "ALL", "SUBMITTED", "UNSUBMITTED"

    private String searchTerm; // Search by staff ID or name

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";
}