package com.hrplatform.service;

import com.hrplatform.dto.request.HrChangePasswordRequest;
import com.hrplatform.dto.request.HrLoginRequest;
import com.hrplatform.dto.request.HrPasswordResetRequest;
import com.hrplatform.dto.request.HrResetPasswordWithTokenRequest;
import com.hrplatform.dto.response.HrLoginResponse;

public interface HrAuthService {

    HrLoginResponse login(HrLoginRequest request);

    void logout(String email);

    void initiatePasswordReset(HrPasswordResetRequest request);

    void resetPasswordWithToken(String token, HrResetPasswordWithTokenRequest request);

    void changePassword(String email, HrChangePasswordRequest request);

    void resetPassword(String email, String newPassword);

    String generatePasswordResetToken(String email);

    boolean validatePasswordResetToken(String token);

    String getEmailFromResetToken(String token);
}