package com.shopease.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    public static PublicKey loadPublicKey(final String pemPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final String Key = readKeyFromResource(pemPath).replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        final byte[] decoded = Base64.getDecoder().decode(Key);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String readKeyFromResource(final String pemPath) throws IOException {
        try (final InputStream is = KeyUtils.class.getClassLoader().getResourceAsStream(pemPath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + pemPath);
            }
            return new String(is.readAllBytes());
        }
    }
}
