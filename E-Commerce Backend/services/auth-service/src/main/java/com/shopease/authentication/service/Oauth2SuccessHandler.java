package com.shopease.authentication.service;

import com.shopease.authentication.entity.RefreshToken;
import com.shopease.authentication.enums.Provider;
import com.shopease.authentication.repository.RefreshTokenRepository;
import com.shopease.security.service.CookieService;
import com.shopease.security.utils.JwtUtil;
import com.shopease.user.entity.Role;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("IN OAUTH2SuccessHandler onAuthenticationSuccess");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("Oauth2User: {}", oAuth2User);
        if (oAuth2User == null) {
            throw new RuntimeException("Oauth2User is null");
        }
        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
            log.info("registrationId: {}", registrationId);
        }
        User user;
        switch (registrationId) {
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
                log.info("googleId: {} | email: {} | name: {} | picture: {} ", googleId, email, name, picture);
                user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .oauthId(googleId)
                                    .email(email)
                                    .username(email)
                                    .name(name)
                                    .image(picture)
                                    .password(null)
                                    .roles(Set.of(Role.USER))
                                    .provider(Provider.GOOGLE)
                                    .enabled(true)
                                    .build();
                            return userRepository.save(newUser);
                        });
            }
            case "github" -> {
                String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();
                String email = (String) oAuth2User.getAttributes().get("email");
                log.info("githubId: {} | email: {} | name: {} | picture: {} ", githubId, email, name, picture);

                if (email == null) {
                    email = name + "@github.com";
                }

                String finalEmail = email;
                user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .oauthId(githubId)
                                    .email(finalEmail)
                                    .username(name)
                                    .name(name)
                                    .image(picture)
                                    .roles(Set.of(Role.USER))
                                    .provider(Provider.GITHUB)
                                    .enabled(true)
                                    .build();
                            return userRepository.save(newUser);
                        });
            }
            default -> throw new CustomResourceNotFoundException("Unsupported Oauth2");
        }
        String jti = jwtUtil.generateJti();
        UUID userId = user.getUserId();
        if (userId != null) {
            refreshTokenService.revokeAllByUserId(user.getUserId());
        }
        RefreshToken rt = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user, rt.getJti());
        log.info("accessToken: {} | refreshToken: {} ", accessToken, refreshToken);
        cookieService.attachAccessTokenCookie(response, accessToken, Duration.ofSeconds(jwtUtil.getAccessTokenExpiration()));
        cookieService.attachRefreshCookie(response, refreshToken, Duration.ofSeconds(jwtUtil.getRefreshTokenExpiration()));
        cookieService.addNoStoreHeaders(response);
        String res = """
                "Login successful",
                "Access Token": %s,
                "Refresh Token: %s,
                """.formatted(accessToken, refreshToken);
        response.getWriter().write(res);
    }
}
