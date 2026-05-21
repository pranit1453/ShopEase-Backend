package com.shopease.authentication.controller;

import com.shopease.authentication.dto.*;
import com.shopease.authentication.service.AdminService;
import com.shopease.authentication.service.AuthService;
import com.shopease.security.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final CookieService cookieService;
    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<SignupResponse> registerUser(@RequestBody final @Valid SignupRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.registerNewUser(request));
    }

    @PostMapping("/admin")
    public ResponseEntity<AdminResponse> onboardAdmin(final @RequestBody AdminRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.onboardNewAdmin(request));
    }

    @PostMapping("/verify-email")
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
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse res = authService.authenticateUser(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authorities: {}", auth != null ? auth.getAuthorities() : null);
        log.debug("Principal: {}", auth != null ? auth.getPrincipal() : null);
        log.debug("Name: {}", auth != null ? auth.getName() : null);
        return ResponseEntity.status(HttpStatus.OK)
                .body(res);
    }


    @PostMapping("/refresh")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> refreshAccessToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        String refreshToken = authService.readRefreshTokenFromRequest(body, request)
                .orElseThrow();
        authService.generateNewRefreshToken(refreshToken, response);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

}
