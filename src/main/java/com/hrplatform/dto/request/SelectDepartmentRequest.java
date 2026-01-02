package com.hrplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectDepartmentRequest {

    @NotBlank(message = "Staff ID is required")
    private String staffIdNumber;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
}