package com.shopease.authentication.service;

import java.security.SecureRandom;

public class OtpGeneration {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = secureRandom.nextInt(CHAR_POOL.length());
            otp.append(CHAR_POOL.charAt(index));
        }
        return otp.toString();
    }

}
