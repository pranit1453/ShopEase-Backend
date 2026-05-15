package com.shopease.authentication.service;

import com.shopease.authentication.dto.*;
import com.shopease.authentication.entity.RefreshToken;
import com.shopease.authentication.enums.OtpType;
import com.shopease.authentication.enums.Provider;
import com.shopease.authentication.exception.*;
import com.shopease.authentication.repository.RefreshTokenRepository;
import com.shopease.redis.service.RedisService;
import com.shopease.role.entity.Role;
import com.shopease.role.exception.RoleNotFoundException;
import com.shopease.role.repository.RoleRepository;
import com.shopease.security.service.CookieService;
import com.shopease.security.utils.JwtUtil;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
import org.springframework.transaction.annotation.Propagation;
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
    private final RoleRepository roleRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SignupResponse registerNewUser(final SignupRequest request) {
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

        // check for customer role
        Role role = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role USER not found"));
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
        return SignupResponse.builder()
                .status(true)
                .message("Check mail for email verification to enable account")
                .build();
    }

    @Override
    public void resendOtpToUser(String email) {
        email = email.trim().toLowerCase();
        log.debug("Email for Resend is: {}", email);
        OtpType type = OtpType.REGISTRATION;

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        log.debug("User with email: {}", user);
        if (user.isEnabled()) {
            log.warn("User is already enabled");
            throw new AccountVerificationException("Account already verified");
        }

        redisService.validateRequest(null, user.getEmail(), type);

        String otp = OtpGeneration.generateOtp();
        String hashOtp = passwordEncoder.encode(otp);
        log.debug("OTP: {}, Hashed OTP: {}", otp, hashOtp);
        redisService.storeOtp(null, user.getEmail(), hashOtp, type);

        VerificationData verificationData = VerificationData.builder()
                .email(user.getEmail())
                .type(type)
                .otp(otp)
                .build();
        log.debug("Verification data {}", verificationData);
        sendOtpToUserForVerification(verificationData);
    }

    private void sendOtpToUserForVerification(final VerificationData verificationData) {
        streamBridge.send("otpVerificationEvent-out-0", verificationData);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void verifyOtp(String email, final String inputOtp) {
        email = email.trim().toLowerCase();
        log.debug("Email for verify is: {}", email);
        OtpType type = OtpType.REGISTRATION;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        log.debug("User with email to verify: {}", user);
        String otp = redisService.getOtp(null, user.getEmail(), type);
        if (otp == null) {
            log.warn("OTP is null");
            throw new OTPValidationException("OTP expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(inputOtp, otp)) {
            redisService.handleInvalidOtp(null, user.getEmail(), type);
            log.warn("Input otp: {} | OTP: {} both are invalid", inputOtp, otp);
            throw new OTPValidationException("Invalid OTP");
        }

        redisService.markVerified(null, user.getEmail(), type);
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }
        redisService.clearAll(null, user.getEmail(), type);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LoginResponse authenticateUser(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                )
        );
        log.debug("Authentication: {}", authentication);
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        log.debug("User: {}", user);
        if (user == null) {
            log.warn("User is null");
            throw new UsernameNotFoundException("User not found");
        }
        if (!user.isEnabled()) {
            log.warn("User account is disabled");
            throw new DisabledException("User is disabled");
        }

        String jti = jwtUtil.generateJti();
        log.debug("JTI is {}", jti);
        var rt = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration())).revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        String accessToken = jwtUtil.generateAccessToken(user);
        log.debug("Access token: {}", accessToken);
        String refreshToken = jwtUtil.generateRefreshToken(user, jti);
        log.debug("Refresh token: {}", refreshToken);
        cookieService.attachAccessTokenCookie(response, accessToken, Duration.ofSeconds(jwtUtil.getAccessTokenExpiration()));
        cookieService.attachRefreshCookie(response, refreshToken, Duration.ofSeconds(jwtUtil.getRefreshTokenExpiration()));
        cookieService.addNoStoreHeaders(response);
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.debug("Roles are: {}", roles);
        return LoginResponse.builder()
                .username(user.getUsername())
                .roles(roles)
                .build();
    }

    @Override
    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // Prefer reading from cookie
        if (request.getCookies() != null) {
            log.info("Reading from Cookies");
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
            log.info("Reading from message body");
            return Optional.of(body.refreshToken());
        }

        // custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            log.info("Reading from custom header");
            return Optional.of(refreshHeader.trim());
        }

        // From Authorization
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("Reading from Authorization header");
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
            log.warn("Invalid refresh token: {}", refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String jti = jwtUtil.getJtiFromRefreshToken(refreshToken);
        log.debug("JTI for new Refresh token is {}", jti);
        UUID userId = UUID.fromString(jwtUtil.getSubjectFromRefreshToken(refreshToken));
        log.debug("User ID is {}", userId);
        RefreshToken token = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(token.getUser().getUserId());
            log.warn("Refresh token {} reused detected", token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token reuse detected");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token {} expired", token);
            throw new TokenExpiredException("Expired refresh token");
        }
        if (!token.getUser().getUserId().equals(userId)) {
            throw new TokenOwnerException("Refresh token not belong to user");
        }
        token.setRevoked(true);
        String newJti = jwtUtil.generateJti();
        token.setReplacedByToken(newJti);
        refreshTokenRepository.save(token);

        User user = token.getUser();
        log.debug("Refresh token for user: {}", user);
        var newRT = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration())).revoked(false)
                .build();

        refreshTokenRepository.save(newRT);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        log.debug("New Access Token: {}", newAccessToken);
        String newRefreshToken = jwtUtil.generateRefreshToken(user, newJti);
        log.debug("New Refresh Token: {}", newRefreshToken);
        cookieService.attachAccessTokenCookie(response, newAccessToken, Duration.ofSeconds(jwtUtil.getAccessTokenExpiration()));
        cookieService.attachRefreshCookie(response, newRefreshToken, Duration.ofSeconds(jwtUtil.getRefreshTokenExpiration()));
        cookieService.addNoStoreHeaders(response);
    }


}
