package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionListResponse {

    private UUID staffId;
    private String staffIdNumber;
    private String fullName;
    private String email;
    private String departmentName;
    private Long documentsSubmitted;
    private Boolean hasSubmissions;
}