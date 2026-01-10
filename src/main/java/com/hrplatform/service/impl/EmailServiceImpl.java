package com.hrplatform.service.impl;

import com.hrplatform.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            String htmlContent = buildPasswordResetEmailHtml(fullName, resetLink);
            sendHtmlEmail(toEmail, "Password Reset Request - " + appName, htmlContent);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String toEmail, String fullName) {
        try {
            String htmlContent = buildPasswordResetConfirmationEmailHtml(fullName);
            sendHtmlEmail(toEmail, "Password Reset Successful - " + appName, htmlContent);
            log.info("Password reset confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", toEmail, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName, String temporaryPassword) {
        try {
            String htmlContent = buildWelcomeEmailHtml(fullName, toEmail, temporaryPassword);
            sendHtmlEmail(toEmail, "Welcome to " + appName, htmlContent);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildPasswordResetEmailHtml(String fullName, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset=\"UTF-8\">" +
                "  <title>Password Reset</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f5f7fa; padding: 20px;\">" +
                "  <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
                "    <tr>" +
                "      <td align=\"center\">" +
                "        <table width=\"600\" style=\"background: #ffffff; padding: 30px; border-radius: 8px;\">" +
                "          <tr>" +
                "            <td>" +
                "              <h2 style=\"color: #333;\">Password Reset Request</h2>" +
                "              <p>Dear <strong>" + fullName + "</strong>,</p>" +
                "              <p>" +
                "                We received a request to reset your password for your" +
                "                <strong>" + appName + "</strong> account." +
                "              </p>" +
                "              <p style=\"text-align: center; margin: 30px 0;\">" +
                "                <a href=\"" + resetLink + "\"" +
                "                   style=\"background-color: #2563eb; color: #ffffff; padding: 12px 24px;" +
                "                          text-decoration: none; border-radius: 5px; display: inline-block;\">" +
                "                  Reset Your Password" +
                "                </a>" +
                "              </p>" +
                "              <p style=\"color: #555;\">" +
                "                ⏰ This link will expire in <strong>24 hours</strong>." +
                "              </p>" +
                "              <p style=\"color: #555;\">" +
                "                If you did not request a password reset, please ignore this email" +
                "                or contact support if you have concerns." +
                "              </p>" +
                "              <p style=\"margin-top: 40px;\">" +
                "                Best regards,<br>" +
                "                <strong>" + appName + " Team</strong>" +
                "              </p>" +
                "              <hr style=\"margin: 30px 0;\">" +
                "              <p style=\"font-size: 12px; color: #888;\">" +
                "                © 2024 " + appName + ". All rights reserved." +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordResetConfirmationEmailHtml(String fullName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset=\"UTF-8\">" +
                "  <title>Password Reset Successful</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f5f7fa; padding: 20px;\">" +
                "  <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
                "    <tr>" +
                "      <td align=\"center\">" +
                "        <table width=\"600\" style=\"background: #ffffff; padding: 30px; border-radius: 8px;\">" +
                "          <tr>" +
                "            <td>" +
                "              <h2 style=\"color: #333;\">✓ Password Reset Successful</h2>" +
                "              <p>Dear <strong>" + fullName + "</strong>,</p>" +
                "              <p>" +
                "                Your password has been successfully reset for your" +
                "                <strong>" + appName + "</strong> account." +
                "              </p>" +
                "              <p style=\"background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; border-radius: 4px; color: #155724;\">" +
                "                🔒 Your account is now secured with your new password." +
                "              </p>" +
                "              <p style=\"background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; border-radius: 4px; color: #721c24;\">" +
                "                ⚠️ <strong>Important:</strong> If you did not make this change, please contact our support team immediately." +
                "              </p>" +
                "              <p style=\"margin-top: 40px;\">" +
                "                Best regards,<br>" +
                "                <strong>" + appName + " Team</strong>" +
                "              </p>" +
                "              <hr style=\"margin: 30px 0;\">" +
                "              <p style=\"font-size: 12px; color: #888;\">" +
                "                © 2024 " + appName + ". All rights reserved." +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }

    private String buildWelcomeEmailHtml(String fullName, String email, String temporaryPassword) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset=\"UTF-8\">" +
                "  <title>Welcome</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f5f7fa; padding: 20px;\">" +
                "  <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
                "    <tr>" +
                "      <td align=\"center\">" +
                "        <table width=\"600\" style=\"background: #ffffff; padding: 30px; border-radius: 8px;\">" +
                "          <tr>" +
                "            <td>" +
                "              <h2 style=\"color: #333;\">🎉 Welcome to " + appName + "!</h2>" +
                "              <p>Dear <strong>" + fullName + "</strong>,</p>" +
                "              <p>" +
                "                Welcome aboard! Your account has been created successfully." +
                "                We're excited to have you on our platform." +
                "              </p>" +
                "              <div style=\"background-color: #e7f3ff; border: 2px solid #2196F3; padding: 20px; border-radius: 8px; margin: 20px 0;\">" +
                "                <h3 style=\"color: #1976D2; margin: 0 0 15px 0;\">🔑 Your Login Credentials</h3>" +
                "                <p style=\"margin: 10px 0;\"><strong>Email:</strong> " + email + "</p>" +
                "                <p style=\"margin: 10px 0;\"><strong>Temporary Password:</strong> " +
                "                  <span style=\"font-family: 'Courier New', monospace; background-color: #f0f0f0; padding: 5px 10px; border-radius: 4px; font-weight: bold;\">" + temporaryPassword + "</span>" +
                "                </p>" +
                "              </div>" +
                "              <p style=\"background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; color: #856404;\">" +
                "                🔐 <strong>Important:</strong> Please change your password after your first login for security reasons." +
                "              </p>" +
                "              <p style=\"color: #555;\">" +
                "                If you have any questions or need assistance, feel free to reach out to our support team." +
                "              </p>" +
                "              <p style=\"margin-top: 40px;\">" +
                "                Best regards,<br>" +
                "                <strong>" + appName + " Team</strong>" +
                "              </p>" +
                "              <hr style=\"margin: 30px 0;\">" +
                "              <p style=\"font-size: 12px; color: #888;\">" +
                "                © 2024 " + appName + ". All rights reserved." +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }
}