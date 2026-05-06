package com.shopease.user.service;

import com.shopease.authentication.dto.VerificationData;
import com.shopease.authentication.enums.OtpType;
import com.shopease.authentication.exception.*;
import com.shopease.authentication.service.OtpGeneration;
import com.shopease.redis.service.RedisService;
import com.shopease.user.dto.*;
import com.shopease.user.entity.User;
import com.shopease.user.mapper.UserMapper;
import com.shopease.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StreamBridge streamBridge;
    private final RedisService redisService;

    private static UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new CustomUnauthorizedException("User not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new CustomUnauthorizedException("Invalid authentication principal");
        }
        UUID userId = user.getUserId();
        if (userId == null) {
            throw new CustomUnauthorizedException("User ID not found in authentication");
        }
        return userId;
    }

    @Override
    @Transactional
    public UpdateUserDetailsResponse updateCompleteUserDetails(final UUID id, final UpdateUserDetailsRequest request) {
        User user = findById(id);
            user.setUsername(request.username());
            user.setName(request.name());
            user.setGender(request.gender());
            User saved = userRepository.save(user);
            return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UpdateUserDetailsResponse patchUserDetails(final UUID id, final UpdateUserDetailsRequest request) {
        User user = findById(id);
            userMapper.patchUserDetails(request, user);
            User saved = userRepository.save(user);
            return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void confirmPasswordChange(final UpdatePasswordRequest request) {
        UUID userId = getUserId();
        User userData = findById(userId);
        if (!passwordEncoder.matches(request.oldPassword(), userData.getPassword())) {
            throw new PasswordValidationException("Old Password Do Not Match");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordValidationException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), userData.getPassword())) {
            throw new PasswordValidationException("New Password must be different from old password");
        }
        userData.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(userData);
    }

    @Override
    @Transactional
    public void requestPasswordChangeOtp(EmailRequest request) {

        log.debug("Email: {} ", request.email());
        String email = request.email().trim().toLowerCase();
        OtpType type = OtpType.PASSWORD_CHANGE;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        log.debug("User: {} ", user);
        redisService.validateRequest(user.getUserId(), user.getEmail(), type);
        String otp = OtpGeneration.generateOtp();
        String hashOtp = passwordEncoder.encode(otp);
        redisService.storeOtp(user.getUserId(), user.getEmail(), hashOtp, type);
        VerificationData data = VerificationData.builder()
                .email(user.getEmail())
                .type(type)
                .otp(otp)
                .build();
        publishPasswordChangeOtpEvent(data);
    }

    @Override
    public void verifyPasswordChangeOtp(VerifyPasswordRequest request) {
        UUID userId = getUserId();
        OtpType type = OtpType.PASSWORD_CHANGE;
        User user = findById(userId);
        String email = user.getEmail();
        String otp = redisService.getOtp(userId, email, type);
        if (otp == null) {
            throw new OTPValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(request.otp(), otp)) {
            redisService.handleInvalidOtp(userId, email, type);
            throw new OTPValidationException("Invalid OTP");
        }
        redisService.markVerified(userId, email, type);
        // Clear OTP attempts but KEEP verification flag
        redisService.clearOtpOnly(userId, email, type);
    }

    @Override
    @Transactional
    public void confirmPasswordChange(@Valid PasswordRequest request) {
        UUID userId = getUserId();
        OtpType type = OtpType.PASSWORD_CHANGE;
        User userData = findById(userId);
        String email = userData.getEmail();
        boolean verified = redisService.isVerified(userData.getUserId(), email, type);
        if (!verified) {
            throw new OTPValidationException("OTP verification required");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordValidationException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), userData.getPassword())) {
            throw new PasswordValidationException("New Password must be different from old password");
        }
        userData.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(userData);
        redisService.clearAll(userData.getUserId(), email, type);
    }

    @Override
    @Transactional
    public void requestEmailChangeOtp(final EmailRequest request) {
        UUID userId = getUserId();
        OtpType type = OtpType.EMAIL_CHANGE;

        User userData = findById(userId);

        String oldEmail = userData.getEmail();
        String newEmail = request.email().trim().toLowerCase();

        if (newEmail.equals(oldEmail)) {
            throw new EmailValidationException("Email to Change should be different from old email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailValidationException("Email already exists");
        }
        redisService.validateRequest(userId, newEmail, type);

        String otp = OtpGeneration.generateOtp();
        String hashOtp = passwordEncoder.encode(otp);
        redisService.storeOtp(userData.getUserId(), newEmail, hashOtp, type);

        VerificationData data = VerificationData.builder()
                .email(newEmail)
                .otp(otp)
                .type(type)
                .build();
        publishEmailChangeOtpEvent(data);
    }

    @Override
    public void verifyEmailChangeOtp(NewEmailRequest request) {
        UUID userId = getUserId();
        String newEmail = request.newEmail().trim().toLowerCase();
        OtpType type = OtpType.EMAIL_CHANGE;

        String otp = redisService.getOtp(userId, newEmail, type);
        if (otp == null) {
            throw new OTPValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(request.otp(), otp)) {
            redisService.handleInvalidOtp(userId, newEmail, type);
            throw new OTPValidationException("Invalid OTP");
        }
        redisService.markVerified(userId, newEmail, type);
        // Clear OTP attempts but KEEP verification flag
        redisService.clearOtpOnly(userId, newEmail, type);
        confirmEmailChange(newEmail);
    }

    @Transactional
    private void confirmEmailChange(final String newEmail) {
        UUID userId = getUserId();
        OtpType type = OtpType.EMAIL_CHANGE;

        User userData = findById(userId);

        String oldEmail = userData.getEmail();

        boolean verified = redisService.isVerified(userData.getUserId(), newEmail, type);
        if (!verified) {
            throw new CustomUnauthorizedException("OTP verification required");
        }

        if (newEmail.equals(userData.getEmail())) {
            throw new EmailValidationException("New Email should be different from old email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailValidationException("Email already in use");
        }
        userData.setEmail(newEmail);
        userRepository.save(userData);

        redisService.clearAll(userData.getUserId(), newEmail, type);
        // notify old email
        EmailData data = EmailData.builder()
                .oldEmail(oldEmail)
                .newEmail(newEmail)
                .build();
        sendSecurityAlertToOldEmail(data);
    }

    private void sendSecurityAlertToOldEmail(EmailData data) {
        streamBridge.send("emailChangeSecurityAlert-out-0", data);
    }

    private void publishEmailChangeOtpEvent(VerificationData data) {
        streamBridge.send("emailChangeOtpNotification-out-0", data);
    }

    private void publishPasswordChangeOtpEvent(VerificationData data) {
        streamBridge.send("passwordChangeOtpEvent-out-0", data);
    }

    private User findById(final UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with ID " + userId + " not found"));
    }
}
