package com.hrplatform.service.impl;

import com.hrplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:HR Document Management Platform}")
    private String appName;

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - " + appName);
            message.setText(buildPasswordResetEmailBody(fullName, resetLink));

            mailSender.send(message);

            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Successful - " + appName);
            message.setText(buildPasswordResetConfirmationEmailBody(fullName));

            mailSender.send(message);

            log.info("Password reset confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", toEmail, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to " + appName);
            message.setText(buildWelcomeEmailBody(fullName, toEmail, temporaryPassword));

            mailSender.send(message);

            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailBody(String fullName, String resetLink) {
        return String.format(
                "Dear %s,\n\n" +
                        "We received a request to reset your password for your %s account.\n\n" +
                        "To reset your password, please click on the link below:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "%s Team",
                fullName, appName, resetLink, appName
        );
    }

    private String buildPasswordResetConfirmationEmailBody(String fullName) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your password has been successfully reset for your %s account.\n\n" +
                        "If you did not make this change, please contact support immediately.\n\n" +
                        "Best regards,\n" +
                        "%s Team",
                fullName, appName, appName
        );
    }

    private String buildWelcomeEmailBody(String fullName, String email, String temporaryPassword) {
        return String.format(
                "Dear %s,\n\n" +
                        "Welcome to %s!\n\n" +
                        "Your account has been created successfully.\n\n" +
                        "Login Credentials:\n" +
                        "Email: %s\n" +
                        "Temporary Password: %s\n\n" +
                        "Please change your password after your first login.\n\n" +
                        "Best regards,\n" +
                        "%s Team",
                fullName, appName, email, temporaryPassword, appName
        );
    }
}