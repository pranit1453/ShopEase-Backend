package com.shopease.authentication.service;

import com.shopease.authentication.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Optional;

public interface AuthService {
    SignupResponse registerNewUser(@Valid SignupRequest request);

    void verifyOtp(String email, String inputOtp);

    LoginResponse authenticateUser(LoginRequest request, HttpServletResponse response);

    Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request);

    void generateNewRefreshToken(String refreshToken, HttpServletResponse response);

    void resendOtpToUser(@Valid String email);
}
