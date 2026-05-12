package com.shopease.authentication.service;

import com.shopease.authentication.dto.AdminRequest;
import com.shopease.authentication.dto.AdminResponse;
import com.shopease.authentication.dto.RoleRequest;
import com.shopease.authentication.exception.RoleAlreadyAssignedException;
import com.shopease.authentication.dto.VerificationData;
import com.shopease.authentication.enums.OtpType;
import com.shopease.authentication.enums.Provider;
import com.shopease.authentication.exception.EmailAlreadyExistsException;
import com.shopease.authentication.exception.PasswordValidationException;
import com.shopease.authentication.exception.UserNotFoundException;
import com.shopease.authentication.exception.UsernameAlreadyExists;
import com.shopease.redis.service.RedisService;
import com.shopease.role.repository.RoleRepository;
import com.shopease.role.exception.RoleNotFoundException;
import com.shopease.role.entity.Role;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final StreamBridge streamBridge;
    private final RoleRepository roleRepository;
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AdminResponse onboardNewAdmin(final AdminRequest request) {
        String email = request.email().trim().toLowerCase();
        OtpType type = OtpType.REGISTRATION;

        if (userRepository.existsByEmail(email)) {
            log.warn("Email: {} already exists", email);
            throw new EmailAlreadyExistsException("Email: " + email + " already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
            log.warn("Username: {} already taken", request.username());
            throw new UsernameAlreadyExists("Username: " + request.username() + " already exists. Try new one");
        }
        if (!request.password().equals(request.confirmPassword())) {
            log.warn("Confirm password and new password are not matching");
            throw new PasswordValidationException("Passwords don't match");
        }
        redisService.validateRequest(null, email, type);
        Role role = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RoleNotFoundException("Role ADMIN not found"));
        User user = User.builder()
                .username(request.username())
                .name(request.name())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .gender(request.gender())
                .roles(Set.of(role))
                .provider(Provider.LOCAL)
                .enabled(false)
                .build();

        User saved = userRepository.save(user);
        log.debug("User Saved: {}", saved);
        String otp = OtpGeneration.generateOtp();
        String hashOtp = passwordEncoder.encode(otp);
        log.debug("OTP: {} | Hashed OTP: {}", otp, hashOtp);

        redisService.storeOtp(null, saved.getEmail(), hashOtp, type);

        VerificationData verificationData = VerificationData.builder()
                .email(saved.getEmail())
                .type(type)
                .otp(otp)
                .build();
        log.debug("Verification data: {}", verificationData);
        sendOtpToUserForVerification(verificationData);
        return AdminResponse.builder()
                .status(true)
                .message("Check mail for email verification to enable account")
                .build();
    }

    private void sendOtpToUserForVerification(final VerificationData verificationData) {
        streamBridge.send("otpVerificationEvent-out-0", verificationData);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void assignedNewRoleToUser(final UUID userId, final RoleRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + userId + " not found"));
        log.debug("User data with userID: {}", userId);
        Role role = roleRepository.findByRoleName(request.role())
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role " + request.role() + " not found"));
        log.debug("Fetched role: {}", role.getRoleName());
        if (user.getRoles().contains(role)) {
            log.warn("Role {} already assigned to user {}",
                    role.getRoleName(),
                    userId);
            throw new RoleAlreadyAssignedException("User already has Role: " + role);
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
