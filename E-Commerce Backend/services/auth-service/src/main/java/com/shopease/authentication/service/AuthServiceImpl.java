package com.shopease.authentication.service;

import com.shopease.authentication.dto.*;
import com.shopease.common.authentication.dto.*;
import com.shopease.authentication.entity.RefreshToken;
import com.shopease.authentication.enums.OtpType;
import com.shopease.authentication.enums.Provider;
import com.shopease.authentication.repository.RefreshTokenRepository;
import com.shopease.security.service.CookieService;
import com.shopease.security.utils.JwtUtil;
import com.shopease.user.entity.Role;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;
    private final StreamBridge streamBridge;

    @Override
    @Transactional
    public SignupResponse registerNewUser(final SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        OtpType type = OtpType.REGISTRATION;

        if (userRepository.existsByEmail(email)) {
            throw new CustomConflictException("Email: " + email + " already exists");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new CustomValidationException("Passwords don't match");
        }
        try {
            redisService.validateRequest(null, email, type);
            User user = User.builder()
                    .username(request.username())
                    .name(request.name())
                    .email(email)
                    .password(passwordEncoder.encode(request.password()))
                    .gender(request.gender())
                    .roles(Set.of(Role.USER))
                    .provider(Provider.LOCAL)
                    .enabled(false)
                    .build();

            User saved = userRepository.save(user);

            String otp = OtpGeneration.generateOtp();
            String hashOtp = passwordEncoder.encode(otp);

            redisService.storeOtp(null, saved.getEmail(), hashOtp, type);

            VerificationData verificationData = VerificationData.builder()
                    .email(saved.getEmail())
                    .type(type)
                    .otp(otp)
                    .build();

            sendOtpToUserForVerification(verificationData);

        } catch (DataIntegrityViolationException exception) {
            throw new CustomDataIntegrityViolationException("Database constraint violation");
        }
        return SignupResponse.builder()
                .status(true)
                .message("Check mail for email verification to enable account")
                .build();
    }

    @Override
    public void resendOtpToUser(String email) {
        email = email.trim().toLowerCase();
        OtpType type = OtpType.REGISTRATION;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomResourceNotFoundException("User not found"));

        if (user.isEnabled()) {
            throw new CustomValidationException("Account already verified");
        }

        redisService.validateRequest(null, user.getEmail(), type);

        String otp = OtpGeneration.generateOtp();
        String hashOtp = passwordEncoder.encode(otp);

        redisService.storeOtp(null, user.getEmail(), hashOtp, type);

        VerificationData verificationData = VerificationData.builder()
                .email(user.getEmail())
                .type(type)
                .otp(otp)
                .build();

        sendOtpToUserForVerification(verificationData);
    }

    private void sendOtpToUserForVerification(final VerificationData verificationData) {
        streamBridge.send("otpVerificationEvent-out-0", verificationData);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, final String inputOtp) {
        email = email.trim().toLowerCase();
        OtpType type = OtpType.REGISTRATION;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomResourceNotFoundException("User not found"));

        String otp = redisService.getOtp(null, user.getEmail(), type);
        if (otp == null) {
            throw new CustomValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(inputOtp, otp)) {
            redisService.handleInvalidOtp(null, user.getEmail(), type);
            throw new CustomValidationException("Invalid OTP");
        }

        redisService.markVerified(null, user.getEmail(), type);
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }
        redisService.clearAll(null, user.getEmail(), type);
    }


    @Override
    @Transactional
    public LoginResponse authenticateUser(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                )
        );
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        String jti = jwtUtil.generateJti();
        var rt = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration())).revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user, jti);
        cookieService.attachAccessTokenCookie(response, accessToken, Duration.ofSeconds(jwtUtil.getAccessTokenExpiration()));
        cookieService.attachRefreshCookie(response, refreshToken, Duration.ofSeconds(jwtUtil.getRefreshTokenExpiration()));
        cookieService.addNoStoreHeaders(response);
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return LoginResponse.builder()
                .username(user.getUsername())
                .roles(roles)
                .build();
    }

    @Override
    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // Prefer reading from cookie
        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieService.getRefreshTokenCookieName().equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }

        // From body
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        // custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        // From Authorization
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    if (jwtUtil.isRefreshToken(token)) {
                        return Optional.of(token);
                    }
                } catch (Exception e) {
                    log.error("Invalid refresh token: {}", token);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void generateNewRefreshToken(String refreshToken, HttpServletResponse response) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String jti = jwtUtil.getJtiFromRefreshToken(refreshToken);
        UUID userId = UUID.fromString(jwtUtil.getSubjectFromRefreshToken(refreshToken));

        RefreshToken token = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(token.getUser().getUserId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token reuse detected");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Expired refresh token");
        }
        if (!token.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Refresh token not belong to user");
        }
        token.setRevoked(true);
        String newJti = jwtUtil.generateJti();
        token.setReplacedByToken(newJti);
        refreshTokenRepository.save(token);

        User user = token.getUser();

        var newRT = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration())).revoked(false)
                .build();

        refreshTokenRepository.save(newRT);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user, newJti);
        cookieService.attachAccessTokenCookie(response, newAccessToken, Duration.ofSeconds(jwtUtil.getAccessTokenExpiration()));
        cookieService.attachRefreshCookie(response, newRefreshToken, Duration.ofSeconds(jwtUtil.getRefreshTokenExpiration()));
        cookieService.addNoStoreHeaders(response);
    }

    @Override
    public void revokedRefreshToken(String token) {
        try {
            if (jwtUtil.isRefreshToken(token)) {
                String jti = jwtUtil.getJtiFromRefreshToken(token);
                refreshTokenRepository.findByJti(jti)
                        .ifPresent(refreshToken -> {
                            refreshToken.setRevoked(true);
                            refreshTokenRepository.save(refreshToken);
                        });
            }
        } catch (JwtException ex) {
            log.error("Invalid refresh token to revoke: {}", token);
        }
    }
}
