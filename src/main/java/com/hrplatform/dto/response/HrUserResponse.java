package com.hrplatform.dto.response;

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
public class HrUserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private Boolean isActive;
    private Boolean isFirstLogin;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}