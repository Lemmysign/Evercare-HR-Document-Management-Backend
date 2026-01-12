package com.hrplatform.service;

import com.hrplatform.dto.request.EmailRequest;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String fullName, String resetLink);

    void sendPasswordResetConfirmationEmail(String toEmail, String fullName);

    void sendWelcomeEmail(String toEmail, String fullName, String temporaryPassword);

    CompletableFuture<Boolean> sendOtpEmail(String toEmail, String staffName, String otpCode);

    CompletableFuture<Boolean> sendEmail(EmailRequest request);

    CompletableFuture<Boolean> sendSimpleEmail(String to, String subject, String text);

    EmailStats getEmailStats();

    record EmailStats(
            long totalSent,
            long totalFailed,
            long pendingQueue,
            double successRate
    ) {}



}