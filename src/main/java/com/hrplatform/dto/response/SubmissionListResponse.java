package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
public class SubmissionListResponse {

    private UUID staffId;
    private String staffIdNumber;
    private String fullName;
    private String email;
    private String departmentName;
    private Long documentsSubmitted;
    private Boolean hasSubmissions;

    public SubmissionListResponse(UUID staffId, String staffIdNumber, String fullName,
                                  String email, String departmentName, Long documentsSubmitted,
                                  Boolean hasSubmissions) {
        this.staffId = staffId;
        this.staffIdNumber = staffIdNumber;
        this.fullName = fullName;
        this.email = email;
        this.departmentName = departmentName;
        this.documentsSubmitted = documentsSubmitted;
        this.hasSubmissions = hasSubmissions;
    }


}

