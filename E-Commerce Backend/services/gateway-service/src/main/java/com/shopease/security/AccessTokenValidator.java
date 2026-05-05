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

    public Claims validateToken(final String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if(!claims.getIssuer().equals(issuer)) {
            throw new RuntimeException("Invalid Access Token");
        }

        if(!"access".equals(String.valueOf(claims.get("token")))) {
            throw new RuntimeException("Invalid Access Token");
        }
        return claims;
    }


}
