package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionToken;
    private Long expiresIn; // seconds
    private String staffName;
    private String departmentName;
}