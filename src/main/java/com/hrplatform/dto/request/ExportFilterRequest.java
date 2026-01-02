package com.hrplatform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportFilterRequest {

    private UUID departmentId; // null = all departments

    private String submissionStatus; // "ALL", "SUBMITTED", "UNSUBMITTED"

    private LocalDateTime dateRangeStart;

    private LocalDateTime dateRangeEnd;
}