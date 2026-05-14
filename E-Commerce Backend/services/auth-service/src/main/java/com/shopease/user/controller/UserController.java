package com.shopease.user.controller;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.user.dto.*;
import com.shopease.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/otp/verify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> verifyOtpForPassword(@RequestBody final @Valid VerifyPasswordRequest request) {
        userService.verifyPasswordChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Otp has been verified");
    }

    @PostMapping("/confirm/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updatePassword(@RequestBody final @Valid PasswordRequest request) {
        userService.confirmPasswordChange(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Password updated successfully");
    }

    @PostMapping("/update/email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> updateEmail(@RequestBody final @Valid EmailRequest request) {
        userService.requestEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Check Email for OTP");
    }

    @PostMapping("/email/verify")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> verifyOtpForEmail(@RequestBody final @Valid NewEmailRequest request) {
        userService.verifyEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("OTP has been verified and Email Updated successfully");
    }

    @GetMapping("/all")
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

    @PutMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<String> updateRole(@PathVariable final UUID userId, @PathVariable final UUID roleId) {
        userService.updateRole(userId, roleId);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Role updated successfully");
    }

//    @PutMapping("/{userId}/role/{roleId}/permissions")
//    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ROLE_UPDATE')")
//    public ResponseEntity<String> updateRole(@PathVariable final UUID userId, @PathVariable final UUID roleId, @PathVariable final UUID permissionId) {
//        userService.updateRermission(userId, roleId, permissionId);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body("Role updated successfully");
//    }
}
