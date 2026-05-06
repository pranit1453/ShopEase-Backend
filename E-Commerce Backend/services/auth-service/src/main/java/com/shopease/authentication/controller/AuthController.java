package com.shopease.authentication.controller;

import com.shopease.authentication.dto.*;
import com.shopease.authentication.service.AuthService;
import com.shopease.security.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<SignupResponse> login(@RequestBody final @Valid SignupRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.registerNewUser(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody final @Valid VerifyRequest request) {
        authService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.status(HttpStatus.OK)
                .body("Email verified successfully");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody final @Valid String email) {
        authService.resendOtpToUser(email);
        return ResponseEntity.status(HttpStatus.OK)
                .body("OTP Resend successfully");
    }

    /**
     * api authenticateUser(username,password) --> call authentication manager --> call authentication provider --> call userDetailService --> call database to fetch data
     * once data is validated then security context holder confirms that request is authenticated
     * after that Access token is generated for client and send back to client
     * in HttpOnlyCookie
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.authenticateUser(request, response));
    }


    @PostMapping("/refresh")
    public ResponseEntity<Boolean> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        String refreshToken = authService.readRefreshTokenFromRequest(body, request)
                .orElseThrow();
        authService.generateNewRefreshToken(refreshToken, response);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.readRefreshTokenFromRequest(null, request).ifPresent(authService::revokedRefreshToken);
        cookieService.clearRefreshTokenCookie(response);
        cookieService.clearAccessTokenCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

//    @PreAuthorize("hasRole('USER')")
//    @PostMapping("/user")
//    public ResponseEntity<String> forUser() {
//        return ResponseEntity.ok("User Calling.......");
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin")
//    public ResponseEntity<String> forAdmin() {
//        return ResponseEntity.ok("Admin Calling.......");
//    }
}
