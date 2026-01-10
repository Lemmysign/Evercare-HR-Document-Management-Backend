package com.hrplatform.service.impl;

import com.hrplatform.dto.request.HrChangePasswordRequest;
import com.hrplatform.dto.request.HrLoginRequest;
import com.hrplatform.dto.request.HrPasswordResetRequest;
import com.hrplatform.dto.request.HrResetPasswordWithTokenRequest;
import com.hrplatform.dto.response.HrLoginResponse;
import com.hrplatform.entity.HrUser;
import com.hrplatform.exception.BadRequestException;
import com.hrplatform.exception.ResourceNotFoundException;
import com.hrplatform.exception.UnauthorizedException;
import com.hrplatform.mapper.HrUserMapper;
import com.hrplatform.repository.HrUserRepository;
import com.hrplatform.security.JwtTokenProvider;
import com.hrplatform.service.AuditService;
import com.hrplatform.service.EmailService;
import com.hrplatform.service.HrAuthService;
import com.hrplatform.service.HrUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrAuthServiceImpl implements HrAuthService {

    private final HrUserService hrUserService;
    private final HrUserRepository hrUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final HrUserMapper hrUserMapper;
    private final AuditService auditService;
    private final EmailService emailService;


    @Value("${frontend.email.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.password-reset-token.expiry-hours:24}")
    private int passwordResetTokenExpiryHours;

    @Override
    @Transactional
    public HrLoginResponse login(HrLoginRequest request) {
        log.info("HR user login attempt: {}", request.getEmail());

        HrUser hrUser = hrUserService.findByEmail(request.getEmail());

        if (!hrUser.getIsActive()) {
            log.warn("Inactive HR user attempted login: {}", request.getEmail());
            throw new UnauthorizedException("User account is inactive");
        }

        String storedPassword = hrUser.getPassword();

        // Check if stored password is BCrypt
        if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$") && !storedPassword.startsWith("$2y$")) {
            log.warn("Plain-text password detected for user {}. Encoding it now.", request.getEmail());
            storedPassword = passwordEncoder.encode(storedPassword);
            hrUser.setPassword(storedPassword);
            hrUserService.save(hrUser); // persist the encoded password
        }

        if (!passwordEncoder.matches(request.getPassword(), storedPassword)) {
            log.warn("Invalid password for HR user: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(hrUser.getEmail());

        hrUserService.updateLastLogin(request.getEmail());

        auditService.logHrActivity(
                hrUser.getEmail(),
                "LOGIN",
                null,
                "HR user logged in successfully"
        );

        log.info("HR user logged in successfully: {}", request.getEmail());

        return hrUserMapper.toLoginResponse(hrUser, token, "Login successful");
    }

    @Override
    @Transactional
    public void logout(String email) {
        log.info("HR user logout: {}", email);

        auditService.logHrActivity(
                email,
                "LOGOUT",
                null,
                "HR user logged out successfully"
        );

        log.info("HR user logged out successfully: {}", email);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(HrPasswordResetRequest request) {
        log.info("Password reset initiated for: {}", request.getEmail());

        if (!hrUserService.existsByEmail(request.getEmail())) {
            log.warn("Password reset attempted for non-existent email: {}", request.getEmail());
            throw new ResourceNotFoundException("Email not found in our system");
        }

        HrUser hrUser = hrUserService.findByEmail(request.getEmail());

        // Generate reset token
        String resetToken = generatePasswordResetToken(request.getEmail());

        // Save token to user
        hrUser.setPasswordResetToken(resetToken);
        hrUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(passwordResetTokenExpiryHours));
        hrUserRepository.save(hrUser);

        // Generate reset link
        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, resetToken);

        // Send email
        emailService.sendPasswordResetEmail(hrUser.getEmail(), hrUser.getFullName(), resetLink);

        auditService.logHrActivity(
                request.getEmail(),
                "PASSWORD_RESET_INITIATED",
                null,
                "Password reset link sent to email"
        );

        log.info("Password reset email sent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void resetPasswordWithToken(String token, HrResetPasswordWithTokenRequest request) {
        log.info("Resetting password with token");

        if (!validatePasswordResetToken(token)) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String email = getEmailFromResetToken(token);
        HrUser hrUser = hrUserService.findByEmail(email);

        // Update password
        hrUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        hrUser.setPasswordResetToken(null);
        hrUser.setPasswordResetTokenExpiry(null);
        hrUser.setIsFirstLogin(false);

        hrUserRepository.save(hrUser);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(hrUser.getEmail(), hrUser.getFullName());

        auditService.logHrActivity(
                email,
                "PASSWORD_RESET_COMPLETED",
                null,
                "Password was reset using reset token"
        );

        log.info("Password reset completed successfully for: {}", email);
    }

    @Override
    @Transactional
    public void changePassword(String email, HrChangePasswordRequest request) {
        log.info("Password change request for: {}", email);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        HrUser hrUser = hrUserService.findByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), hrUser.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        hrUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        if (hrUser.getIsFirstLogin()) {
            hrUserService.markFirstLoginComplete(email);
        }

        hrUserRepository.save(hrUser);

        auditService.logHrActivity(
                email,
                "PASSWORD_CHANGED",
                null,
                "Password changed successfully"
        );

        log.info("Password changed successfully for: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        log.info("Resetting password for: {}", email);

        HrUser hrUser = hrUserService.findByEmail(email);
        hrUser.setPassword(passwordEncoder.encode(newPassword));
        hrUser.setIsFirstLogin(true);

        hrUserRepository.save(hrUser);

        auditService.logHrActivity(
                email,
                "PASSWORD_RESET",
                null,
                "Password was reset"
        );

        log.info("Password reset successfully for: {}", email);
    }

    @Override
    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        HrUser hrUser = hrUserRepository.findByPasswordResetToken(token)
                .orElse(null);

        if (hrUser == null) {
            return false;
        }

        if (hrUser.getPasswordResetTokenExpiry() == null) {
            return false;
        }

        return hrUser.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public String getEmailFromResetToken(String token) {
        HrUser hrUser = hrUserRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        return hrUser.getEmail();
    }
}