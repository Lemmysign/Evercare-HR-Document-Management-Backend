package com.hrplatform.mapper;

import com.hrplatform.dto.response.HrLoginResponse;
import com.hrplatform.dto.response.HrUserResponse;
import com.hrplatform.entity.HrUser;
import org.springframework.stereotype.Component;

@Component
public class HrUserMapper {

    public HrLoginResponse toLoginResponse(HrUser hrUser, String token, String message) {
        return HrLoginResponse.builder()
                .userId(hrUser.getId())
                .email(hrUser.getEmail())
                .fullName(hrUser.getFullName())
                .token(token)
                .isFirstLogin(hrUser.getIsFirstLogin())
                .lastLoginAt(hrUser.getLastLoginAt())
                .message(message)
                .build();
    }

    public HrUserResponse toResponse(HrUser hrUser) {
        return HrUserResponse.builder()
                .id(hrUser.getId())
                .email(hrUser.getEmail())
                .fullName(hrUser.getFullName())
                .isActive(hrUser.getIsActive())
                .isFirstLogin(hrUser.getIsFirstLogin())
                .createdAt(hrUser.getCreatedAt())
                .lastLoginAt(hrUser.getLastLoginAt())
                .build();
    }
}