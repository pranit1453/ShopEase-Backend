package com.shopease.authentication.service;

import com.shopease.authentication.dto.*;
import com.shopease.authentication.enums.OtpType;
import com.shopease.authentication.enums.Provider;
import com.shopease.authentication.exception.*;
import com.shopease.authentication.repository.RefreshTokenRepository;
import com.shopease.permission.entity.Permission;
import com.shopease.permission.exception.PermissionAlreadyAssignedException;
import com.shopease.permission.exception.PermissionNotFoundException;
import com.shopease.permission.repository.PermissionRepository;
import com.shopease.redis.service.RedisService;
import com.shopease.role.entity.Role;
import com.shopease.role.exception.RoleNotAssignedException;
import com.shopease.role.exception.RoleNotFoundException;
import com.shopease.role.repository.RoleRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PermissionRepository permissionRepository;

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
    public void assignedNewRoleToUser(final UUID userId, final AssignRoleRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + userId + " not found"));
        log.debug("User data with userID: {}", userId);
        Role role = roleRepository.findByRoleName(request.roleName())
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role " + request.roleName() + " not found"));
        log.debug("Fetched role: {}", role.getRoleName());
        if (user.getRoles().contains(role)) {
            log.warn("Role {} already assigned to user {}",
                    role.getRoleName(),
                    userId);
            throw new RoleAlreadyAssignedException("User already has Role: " + role);
        }
        user.getRoles().add(role);
        userRepository.save(user);
        redisService.deleteTokenIdentifier(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void assignPermissionToRole(UUID roleId, AssignPermissionRequest request) {
        Role role = roleRepository.findByRoleId(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role with id " + roleId + " not found"));
        List<Permission> permission = permissionRepository.findByPermissionCodeIn(request.permissionCodes());

        Set<String> foundCodes = permission.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());

        List<String> missingCodes = request.permissionCodes().stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();
        if (!missingCodes.isEmpty()) {
            throw new PermissionNotFoundException("Permission not found with codes: " + missingCodes);
        }
        role.getPermissions().addAll(permission);
        roleRepository.save(role);
    }

    @Override
    public void revokeRoleAssociatedToUser(UUID userId, RevokeRoleRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + userId + " not found"));

        UUID roleId = request.roleId();
        boolean removed = user.getRoles().removeIf(role ->
                role.getRoleId().equals(roleId)
        );
        if (!removed) {
            throw new RoleNotAssignedException(
                    "Role with id " + roleId + " is not assigned to user"
            );
        }
        userRepository.save(user);
        redisService.deleteTokenIdentifier(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    public void assignPermissionToUser(UUID userId, AssignPermissionRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + userId + " not found"));

        Role role = user.getRoles()
                .stream()
                .filter(r -> r.getRoleId().equals(request.roleId()))
                .findFirst()
                .orElseThrow(() ->
                        new RoleNotAssignedException("Role is not assigned to user"));

        List<Permission> permissions = permissionRepository
                .findByPermissionCodeIn(request.permissionCodes());

        if (permissions.isEmpty()) {
            throw new RuntimeException("No permissions found");
        }
        Set<String> existingPermission = role.getPermissions()
                .stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());

        List<String> alreadyAssigned = permissions.stream()
                .map(Permission::getPermissionCode)
                .filter(existingPermission::contains)
                .toList();

        if (!alreadyAssigned.isEmpty()) {
            throw new PermissionAlreadyAssignedException("Permission already assigned to user: " + alreadyAssigned);
        }

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
        redisService.deleteTokenIdentifier(userId);
        refreshTokenRepository.revokeAllByUserId(userId);

    }

    @Override
    @Transactional
    public void revokePermissionAssociatedToUser(UUID userId, RevokePermissionRequest request) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + userId + " not found"));

        Set<String> permissionCodes = new HashSet<>(request.permissionCodes());

        user.getRoles().forEach(role -> {
            role.getPermissions().removeIf(permission ->
                    permissionCodes.contains(permission.getPermissionCode()));
        });
        userRepository.save(user);
        redisService.deleteTokenIdentifier(userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
