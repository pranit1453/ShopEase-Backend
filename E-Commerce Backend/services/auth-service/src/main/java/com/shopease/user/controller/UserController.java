package com.shopease.user.controller;

import com.shopease.user.dto.*;
import com.shopease.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // @PreAuthorize("(hasRole('USER') and @userSecurityService.isUserAuthorized(#userId))")
    public ResponseEntity<UpdateUserDetailsResponse> updateUserDetails(@PathVariable final @NotNull UUID userId, @RequestBody final @Valid UpdateUserDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.updateCompleteUserDetails(userId, request));
    }

    @PostMapping("/patch/{userId}")
    public ResponseEntity<UpdateUserDetailsResponse> patchUserDetails(@PathVariable final @NotNull UUID userId, @RequestBody final @Valid UpdateUserDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.patchUserDetails(userId, request));
    }

    @PostMapping("/update/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid final UpdatePasswordRequest request) {
        userService.confirmPasswordChange(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Password updated successfully");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> changePasswordUsingOtp(@RequestBody final @Valid EmailRequest request) {
        log.debug("Change password using OTP");
        userService.requestPasswordChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Otp has been sent");
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<String> verifyOtpForPassword(@RequestBody final @Valid VerifyPasswordRequest request) {
        userService.verifyPasswordChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Otp has been verified");
    }

    @PostMapping("/confirm/password")
    public ResponseEntity<String> updatePassword(@RequestBody final @Valid PasswordRequest request) {
        userService.confirmPasswordChange(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Password updated successfully");
    }

    @PostMapping("/update/email")
    public ResponseEntity<String> updateEmail(@RequestBody final @Valid EmailRequest request) {
        userService.requestEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Check Email for OTP");
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyOtpForEmail(@RequestBody final @Valid NewEmailRequest request) {
        userService.verifyEmailChangeOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("OTP has been verified and Email Updated successfully");
    }

//    @PostMapping("/confirm")
//    public ResponseEntity<String> confirmEmail(@RequestBody final @Valid EmailRequest request) {
//        userService.confirmEmailChange(request);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body("Email has been updated successfully");
//    }
}
