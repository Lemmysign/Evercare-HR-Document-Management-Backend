package com.hrplatform.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrplatform.util.NullableUUIDDeserializer;
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

    @JsonDeserialize(using = NullableUUIDDeserializer.class)
    private UUID departmentId; // null = all departments

    private String submissionStatus; // "ALL", "SUBMITTED", "UNSUBMITTED"

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateRangeStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateRangeEnd;
}