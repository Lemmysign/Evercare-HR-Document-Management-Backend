package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffValidationResponse {

    private UUID staffId;
    private String staffIdNumber;
    private String fullName;
    private String email;
    private UUID departmentId;
    private String departmentName;
    private Boolean isValid;
    private Boolean hasDepartment; // NEW: Indicates if staff has selected department
    private String message;
    private List<DepartmentOption> availableDepartments; // Available departments to choose from

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentOption {
        private UUID departmentId;
        private String departmentName;
        private String description;
    }
}