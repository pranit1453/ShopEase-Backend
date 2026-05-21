package com.shopease.user.controller;

import com.shopease.authentication.dto.AssignPermissionRequest;
import com.shopease.authentication.dto.AssignRoleRequest;
import com.shopease.authentication.dto.RevokePermissionRequest;
import com.shopease.authentication.dto.RevokeRoleRequest;
import com.shopease.authentication.service.AdminService;
import com.shopease.common.wrapper.PageResponse;
import com.shopease.security.service.CookieService;
import com.shopease.user.dto.*;
import com.shopease.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    public final UserService userService;
    private final CookieService cookieService;
    private final AdminService adminService;

    @PutMapping("/update/{userId}")
    @PreAuthorize("(hasRole('USER') and @userSecurityService.isUserAuthorized(#userId))")
    public ResponseEntity<UpdateUserDetailsResponse> updateUserDetails(@PathVariable final @NotNull UUID userId, @RequestBody final @Valid UpdateUserDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateCompleteUserDetails(userId, request));
    }

    @PostMapping("/patch/{userId}")
    @PreAuthorize(("(hasRole('USER) and @userSecurityService.isUserAuthorized(#userId)"))
    public ResponseEntity<UpdateUserDetailsResponse> patchUserDetails(@PathVariable final @NotNull UUID userId, @RequestBody final @Valid UpdateUserDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.patchUserDetails(userId, request));
    }

    @PostMapping("/update/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid final UpdatePasswordRequest request) {
        userService.confirmPasswordChange(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Password updated successfully");
    }

    @PostMapping("/send-otp")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> changePasswordUsingOtp(@RequestBody final @Valid EmailRequest request) {
        log.debug("Change password using OTP");
        userService.requestPasswordChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Otp has been sent");
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> verifyOtpForPassword(@RequestBody final @Valid VerifyPasswordRequest request) {
        userService.verifyPasswordChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Otp has been verified");
    }

    @PostMapping("/confirm-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updatePassword(@RequestBody final @Valid PasswordRequest request) {
        userService.confirmPasswordChange(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Password updated successfully");
    }

    @PostMapping("/update-email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updateEmail(@RequestBody final @Valid EmailRequest request) {
        userService.requestEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Check Email for OTP");
    }

    @PostMapping("/verify-email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> verifyOtpForEmail(@RequestBody final @Valid NewEmailRequest request) {
        userService.verifyEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("OTP has been verified and Email Updated successfully");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('USER_READ')")
    public ResponseEntity<PageResponse<UserResponse>> fetchAllUserData(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "username") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getAllUsersWithRolesAndPermission(page, size, keyword, sortBy, sortDirection));
    }

    @GetMapping("/user-information")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') and hasAuthority('USER_READ')")
    public ResponseEntity<UserResponse> getUserData() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserWithRolesAndPermission());
    }

    @PutMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_WRITE')")
    public ResponseEntity<String> updateRole(@PathVariable final UUID userId, @PathVariable final UUID roleId) {
        userService.updateRole(userId, roleId);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Role updated successfully");
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        userService.readRefreshTokenFromRequest(null, request).ifPresent(userService::revokedRefreshToken);
        userService.readAccessTokenFromRequest(request).ifPresent(userService::revokedAccessToken);
        cookieService.clearRefreshTokenCookie(response);
        cookieService.clearAccessTokenCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/users/{userId}/assign")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_WRITE')")
    public ResponseEntity<String> assignRoleToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        adminService.assignedNewRoleToUser(userId, request);
        return ResponseEntity.ok("Role assigned successfully");
    }

    @PostMapping("/{roleId}/permissions/assign")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<String> assignPermissionToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionRequest request
    ) {
        adminService.assignPermissionToRole(roleId, request);
        return ResponseEntity.ok("Permissions assigned successfully");
    }

    @DeleteMapping("/{userId}/permissions/revoke")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<String> revokePermissionAssociatedToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody RevokePermissionRequest request
    ) {

        adminService.revokePermissionAssociatedToUser(userId, request);

        return ResponseEntity.ok(
                "Permissions revoked successfully from user roles"
        );
    }

    @PostMapping("/{userId}/roles/revoke")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> revokeRoleAssociatedToUser(
            @PathVariable UUID userId,
            @RequestBody RevokeRoleRequest request
    ) {

        adminService.revokeRoleAssociatedToUser(userId, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/permissions/assign")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<Void> assignPermissionToUser(
            @PathVariable UUID userId,
            @RequestBody AssignPermissionRequest request
    ) {

        adminService.assignPermissionToUser(userId, request);

        return ResponseEntity.noContent().build();
    }
}
