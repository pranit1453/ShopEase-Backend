package com.shopease.service;

import com.shopease.common.dto.EmailData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SecurityAlertNotificationService {

    private final EmailService emailService;

    @Bean
    public Consumer<EmailData> emailChangeSecurityAlert() {
        return (this::handleEmailChangeAlert);
    }

    private void handleEmailChangeAlert(EmailData data) {
        String to = data.oldEmail();
        String subject = "Security Alert: Your Email Has Been Changed";
        String body = buildEmailBody(data);
        emailService.sendEmail(to, subject, body);
    }

    private String buildEmailBody(EmailData data) {
        return """
                Hello,
                
                This is a security alert to inform you that your account email has been successfully changed.
                
                If you made this change, no further action is required.
                
                If you did NOT perform this action, please secure your account immediately by:
                - Resetting your password
                - Contacting support
                
                Your account email has been successfully changed from %s to %s.
                
                Regards,
                Security Team
                """.formatted(data.oldEmail(), data.newEmail());
    }
}
