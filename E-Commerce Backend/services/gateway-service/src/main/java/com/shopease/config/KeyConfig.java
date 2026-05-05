package com.shopease.config;

import com.shopease.utils.KeyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@Configuration
public class KeyConfig {

    @Bean
    public PublicKey publicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyUtils.loadPublicKey("Keys/public_key.pem");
    }
}
