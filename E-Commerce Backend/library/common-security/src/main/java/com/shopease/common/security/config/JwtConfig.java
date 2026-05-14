package com.shopease.common.security.config;

import com.shopease.common.security.util.KeyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {

        RSAPublicKey publicKey = (RSAPublicKey) KeyUtils.loadPublicKey("keys/public_key.pem");

        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }
}
