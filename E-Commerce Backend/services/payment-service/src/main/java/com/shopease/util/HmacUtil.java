package com.shopease.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HmacUtil {

    private final String SECRET;

    public HmacUtil(@Value("${signature.key}") String secret) {
        this.SECRET = secret;
    }

    public String generateSignature(String data) {
        try {

            if (data == null) {
                throw new IllegalArgumentException("data cannot null");
            }
            Mac mac = Mac.getInstance("HmacSHA256");

            byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET);
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
