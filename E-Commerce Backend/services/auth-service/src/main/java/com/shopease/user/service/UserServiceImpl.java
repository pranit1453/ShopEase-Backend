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
import org.springframework.transaction.annotation.Propagation;
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
            log.warn("User is not authenticated: {}", auth);
            throw new UnauthorizedException("User not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            log.warn("Invalid authentication principal: {}", principal);
            throw new UnauthorizedException("Invalid authentication principal");
        }
        UUID userId = user.getUserId();
        if (userId == null) {
            log.warn("User not authenticated with ID: {}", userId);
            throw new UnauthorizedException("User ID not found in authentication");
        }
        return userId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UpdateUserDetailsResponse updateCompleteUserDetails(final UUID id, final UpdateUserDetailsRequest request) {
        User user = findById(id);
        log.debug("User with id: {}|{}", id, user);
        user.setUsername(request.username());
        user.setName(request.name());
        user.setGender(request.gender());
        User saved = userRepository.save(user);
        log.debug("Saved User: {}", saved);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UpdateUserDetailsResponse patchUserDetails(final UUID id, final UpdateUserDetailsRequest request) {
        User user = findById(id);
        log.debug("User with id: {}, {}", id, user);
        userMapper.patchUserDetails(request, user);
        User saved = userRepository.save(user);
        log.debug("User from database: {}", saved);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void confirmPasswordChange(final UpdatePasswordRequest request) {
        UUID userId = getUserId();
        User userData = findById(userId);
        log.debug("User data for password change: {}", userData);
        if (!passwordEncoder.matches(request.oldPassword(), userData.getPassword())) {
            log.warn("Old Password is incorrect");
            throw new PasswordValidationException("Old Password Do Not Match");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("New Password Do Not Match with Confirm Password");
            throw new PasswordValidationException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), userData.getPassword())) {
            log.warn("New Password must be different from old password");
            throw new PasswordValidationException("New Password must be different from old password");
        }
        userData.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(userData);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
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
            log.warn("OTP has been expired");
            throw new OTPValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(request.otp(), otp)) {
            redisService.handleInvalidOtp(userId, email, type);
            log.warn("OTP is Invalid");
            throw new OTPValidationException("Invalid OTP");
        }
        redisService.markVerified(userId, email, type);
        // Clear OTP attempts but KEEP verification flag
        redisService.clearOtpOnly(userId, email, type);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void confirmPasswordChange(@Valid PasswordRequest request) {
        UUID userId = getUserId();
        OtpType type = OtpType.PASSWORD_CHANGE;
        User userData = findById(userId);
        String email = userData.getEmail();
        boolean verified = redisService.isVerified(userData.getUserId(), email, type);
        if (!verified) {
            log.warn("OTP is not verified");
            throw new OTPValidationException("OTP verification required");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("Both password are not matching");
            throw new PasswordValidationException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), userData.getPassword())) {
            log.warn("New password must be different from old password");
            throw new PasswordValidationException("New Password must be different from old password");
        }
        userData.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(userData);
        redisService.clearAll(userData.getUserId(), email, type);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void requestEmailChangeOtp(final EmailRequest request) {
        UUID userId = getUserId();
        OtpType type = OtpType.EMAIL_CHANGE;
        User userData = findById(userId);
        String oldEmail = userData.getEmail();
        String newEmail = request.email().trim().toLowerCase();
        log.debug("Old Email: {} | New Email: {}", oldEmail, newEmail);
        if (newEmail.equals(oldEmail)) {
            log.warn("New email should be different from old email");
            throw new EmailAlreadyExistsException("Email to Change should be different from old email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            log.warn("Email to change already exists");
            throw new EmailAlreadyExistsException("Email already exists");
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
        log.debug("New Email is: {}", newEmail);
        OtpType type = OtpType.EMAIL_CHANGE;

        String otp = redisService.getOtp(userId, newEmail, type);
        if (otp == null) {
            log.warn("OTP is Expired");
            throw new OTPValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(request.otp(), otp)) {
            redisService.handleInvalidOtp(userId, newEmail, type);
            log.warn("OTP is Invalid.");
            throw new OTPValidationException("Invalid OTP");
        }
        redisService.markVerified(userId, newEmail, type);
        // Clear OTP attempts but KEEP verification flag
        redisService.clearOtpOnly(userId, newEmail, type);
        confirmEmailChange(newEmail);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void confirmEmailChange(final String newEmail) {
        UUID userId = getUserId();
        OtpType type = OtpType.EMAIL_CHANGE;

        User userData = findById(userId);

        String oldEmail = userData.getEmail();
        log.debug("Old Email is: {}", oldEmail);
        boolean verified = redisService.isVerified(userData.getUserId(), newEmail, type);
        if (!verified) {
            log.warn("OTP verification is remaining");
            throw new UnauthorizedException("OTP verification required");
        }

        if (newEmail.equals(userData.getEmail())) {
            log.warn("New Email must be different from old email");
            throw new EmailAlreadyExistsException("New Email should be different from old email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            log.warn("Email to change already exists try another one");
            throw new EmailAlreadyExistsException("Email already in use");
        }
        userData.setEmail(newEmail);
        userRepository.save(userData);

        redisService.clearAll(userData.getUserId(), newEmail, type);
        // notify old email
        EmailData data = EmailData.builder()
                .oldEmail(oldEmail)
                .newEmail(newEmail)
                .build();
        log.debug("From Notification Email data is: {}", data);
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
