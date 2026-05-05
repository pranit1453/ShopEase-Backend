package com.shopease.service;

import com.shopease.common.dto.VerificationData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RegistrationOtpNotificationService {

    private final EmailService emailService;

    @Bean
    public Consumer<VerificationData> registrationOtpNotification() {
        return (this::handleRegistrationOtp);
    }

    private void handleRegistrationOtp(final VerificationData verificationData) {
        String to = verificationData.email();
        String subject = "OTP Verification Notification For " + verificationData.type();
        String body = buildEmailBody(verificationData.otp());
        emailService.sendEmail(to, subject, body);
    }

    private String buildEmailBody(final String otp) {
        return String.format("""
                Email Verification
                
                Use the following OTP to verify your email:
                
                %s
                
                This OTP is valid for 5 minutes.
                
                Do not share this OTP with anyone.
                
                If you did not request this, please ignore this email.
                
                Regards,
                Authentication Service
                """, otp);
    }
}
