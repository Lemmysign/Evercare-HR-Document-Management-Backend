package com.hrplatform.controller;

import com.hrplatform.dto.request.HrChangePasswordRequest;
import com.hrplatform.dto.request.HrLoginRequest;
import com.hrplatform.dto.request.HrPasswordResetRequest;
import com.hrplatform.dto.request.HrResetPasswordWithTokenRequest;
import com.hrplatform.dto.response.ApiResponse;
import com.hrplatform.dto.response.HrLoginResponse;
import com.hrplatform.service.HrAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HR Authentication", description = "HR user authentication endpoints")
public class HrAuthController {

    private final HrAuthService hrAuthService;

    @PostMapping("/login")
    @Operation(summary = "HR user login", description = "Authenticate HR user and get JWT token")
    public ResponseEntity<ApiResponse<HrLoginResponse>> login(@Valid @RequestBody HrLoginRequest request) {

        log.info("HR login request for: {}", request.getEmail());

        HrLoginResponse response = hrAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "HR user logout", description = "Logout HR user (Authenticated)")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {

        String email = authentication.getName();
        log.info("HR logout request for: {}", email);

        hrAuthService.logout(email);

        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @PostMapping("/password-reset/initiate")
    @Operation(summary = "Initiate password reset", description = "Request password reset link via email")
    public ResponseEntity<ApiResponse<Void>> initiatePasswordReset(
            @Valid @RequestBody HrPasswordResetRequest request) {

        log.info("Password reset initiated for: {}", request.getEmail());

        hrAuthService.initiatePasswordReset(request);

        return ResponseEntity.ok(ApiResponse.success(null,
                "If the email exists, a password reset link has been sent"));
    }

    @PostMapping("/password-reset/complete")
    @Operation(summary = "Complete password reset", description = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPasswordWithToken(
            @RequestParam String token,
            @Valid @RequestBody HrResetPasswordWithTokenRequest request) {

        log.info("Password reset with token");

        hrAuthService.resetPasswordWithToken(token, request);

        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
    }

    /*@PostMapping("/password/change")
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Change password", description = "Change HR user password (Authenticated)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody HrChangePasswordRequest request) {

        String email = authentication.getName();
        log.info("Password change request for: {}", email);

        hrAuthService.changePassword(email, request);

        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }*/

    @GetMapping("/password-reset/validate-token")
    @Operation(summary = "Validate reset token", description = "Check if password reset token is valid")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {

        log.info("Validating password reset token");

        boolean isValid = hrAuthService.validatePasswordResetToken(token);

        return ResponseEntity.ok(ApiResponse.success(isValid,
                isValid ? "Token is valid" : "Token is invalid or expired"));
    }
}