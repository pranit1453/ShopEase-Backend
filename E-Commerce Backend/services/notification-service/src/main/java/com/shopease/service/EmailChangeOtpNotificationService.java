package com.shopease.service;

import com.shopease.common.dto.VerificationData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class EmailChangeOtpNotificationService {

    private final EmailService emailService;

    @Bean
    public Consumer<VerificationData> emailChangeOtpNotification() {
        return (this::handleEmailChangeOtp);
    }

    private void handleEmailChangeOtp(VerificationData verificationData) {
        String to = verificationData.email();
        String subject = "OTP Notification For " + verificationData.type();
        String body = buildEmailBody(verificationData.otp());
        emailService.sendEmail(to, subject, body);
    }

    private String buildEmailBody(String otp) {
        return String.format("""
                Email Change Request
                
                Use the following OTP to change your Email:
                
                %s
                
                This OTP is valid for 5 minutes.
                If you did not request this, please ignore.
                
                Regards,
                Authentication Service
                """, otp);
    }
}
