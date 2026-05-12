package com.shopease.config;

import com.shopease.utils.KeyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Parsing the JWT Token for Validation is this correct token to forward the request to
 * respective microservices.
 */
@Configuration
public class KeyConfig {

    @Bean
    public PublicKey publicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyUtils.loadPublicKey("Keys/public_key.pem");
    }
}
