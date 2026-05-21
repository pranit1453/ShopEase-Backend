package com.shopease.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class AccessTokenValidator {

    private final PublicKey publicKey;
    private final String issuer;

    public AccessTokenValidator(PublicKey publicKey, @Value("${security.app.issuer}") String issuer) {
        this.publicKey = publicKey;
        this.issuer = issuer;
    }

    public void validateToken(final String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!issuer.equals(claims.getIssuer())) {
                throw new RuntimeException("Invalid issuer");
            }

            if (!"access".equals(String.valueOf(claims.get("token")))) {
                throw new RuntimeException("Invalid token type");
            }
        } catch (Exception e) {
            throw new RuntimeException("JWT validation failed: " + e.getMessage(), e);
        }
    }


}
