package com.shopease.security.utils;

import com.shopease.user.entity.User;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
public class JwtUtil {
    //private final String secret;
    private final long accessTokenExpiration;
    private final String issuer;
    private final long refreshTokenExpiration;
    private final String accessTokenCookieName;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtUtil(
            // @Value("${spring.security.secret.key}") String secret,
            @Value("${spring.security.secret.expiration}") long accessTokenExpiration,
            @Value("${spring.security.secret.issuer}") String issuer,
            @Value("${spring.security.refresh.expiration}") long refreshTokenExpiration,
            @Value("${spring.security.cookie.name.accessTokenName}") String accessTokenCookieName
    ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.issuer = issuer;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.accessTokenCookieName = accessTokenCookieName;
        this.privateKey = KeyUtils.loadPrivateKey("Keys/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("Keys/public_key.pem");
    }

    public String generateJti() {
        SecureRandom RANDOM = new SecureRandom();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // In Production Keep Only One Header Or Cookie
    public String resolveAccessToken(HttpServletRequest request) {
        String headerToken = getAccessTokenFromHeader(request);
        if (headerToken != null && !headerToken.isBlank()) {
            return headerToken;
        }
        return getAccessTokenFromCookie(request);
    }

    public String getAccessTokenFromHeader(final HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.debug("Authorization header: {}", token);
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            return jwt.isBlank() ? null : jwt;
        }
        return null;
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(accessTokenCookieName))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    public String generateAccessToken(final User user) {
        // String username = user.getUsername();
        UUID userId = user.getUserId();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiration);
        Set<String> roles = user.getRoles() == null ? Set.of()
                : user.getRoles()
                  .stream()
                  .map(Roless::name)
                  .collect(Collectors.toSet());
//        Set<String> permission = user.getRoles()
//                .stream()
//                .flatMap(role ->
//                        role.getPermissions().stream())
//                .map(Permission::name)
//                .collect(Collectors.toSet());
        return Jwts.builder()
                .id(id(user.getUserId()))
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(Map.of(
                        "role", roles,
                        "permission", permission,
                        "token", "access"
                ))
                .signWith(this.privateKey)
                .compact();
    }

    public String generateRefreshToken(final User user, String jti) {
        //String username = user.getUsername();
        UUID userId = user.getUserId();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenExpiration);
        return Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(Map.of(
                        "token", "refresh"
                ))
                .signWith(this.privateKey)
                .compact();
    }

    private Jws<Claims> parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.publicKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    public String getJtiFromRefreshToken(final String token) {
        return parse(token)
                .getPayload()
                .getId();
    }

    public String getSubjectFromRefreshToken(final String token) {
        return parse(token)
                .getPayload()
                .getSubject();
    }


    public String getSubjectFromAccessToken(final String token) {
        return parse(token)
                .getPayload()
                .getSubject();
    }

    public boolean isAccessToken(final String token) {
        Claims claims = parse(token)
                .getPayload();
        return "access".equals(String.valueOf(claims.get("token")));
    }

    public boolean isRefreshToken(final String token) {
        Claims claims = parse(token)
                .getPayload();
        return "refresh".equals(String.valueOf(claims.get("token")));
    }

    public boolean validateAccessToken(final String authToken) {
        try {
            parse(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT Token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT Token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT Token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT Claims String is Empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected Exception: {}", ex.getMessage());
        }
        return false;
    }

    private String id(final UUID userId) {
        final SecureRandom RANDOM = new SecureRandom();
        String userPart = userId.toString().replace("-", "").substring(0, 8);
        long timestamp = Instant.now().toEpochMilli();
        int random = 1000 + RANDOM.nextInt(9000);
        return userPart + timestamp + random;
    }

//    private SecretKey key() {
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
//    }
}

// hld and lld , 2 user flows, documentation