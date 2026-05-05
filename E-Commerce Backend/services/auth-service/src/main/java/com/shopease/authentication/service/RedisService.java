package com.shopease.authentication.service;

import com.shopease.authentication.enums.OtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final int MAX_REQUESTS = 3;
    private static final int MAX_VERIFY_ATTEMPTS = 3;
    private static final long OTP_TTL = 180; // 3 min
    private static final long RESEND_TTL = 60; // 1 min
    private static final long BLOCK_TTL = 600; // 10 min
    private static final long VERIFIED_TTL = 600; // 10 min

    private final StringRedisTemplate redisTemplate;

    // Keys builder

    private String key(String prefix, OtpType type, String identifier) {
        return prefix + ":" + type + ":" + identifier;
    }

    private String identifier(UUID userId, String email) {
        return (userId != null)
                ? userId + ":" + email
                : email;
    }

    private long toMinutes(Long ttl) {
        if (ttl == null || ttl <= 0) return 1; // avoid 0 minutes
        return (ttl + 59) / 60; // round up
    }

    public void validateRequest(UUID userId, String email, OtpType type) {
        String id = identifier(userId, email);
        String blockKey = key("otp:block", type, id);
        String resendKey = key("otp:resend", type, id);
        String reqKey = key("otp:req", type, id);

        //  BLOCK CHECK (10 min)
        Long blockTtl = redisTemplate.getExpire(blockKey);
        if (blockTtl != null && blockTtl > 0) {
            throw new CustomValidationException(
                    "Blocked. Try again after " + toMinutes(blockTtl) + " minute(s)"
            );
        }
        // RESEND CHECK (1 min)
        Long resendTtl = redisTemplate.getExpire(resendKey);
        if (resendTtl != null && resendTtl > 0) {
            throw new CustomValidationException(
                    "Please wait " + toMinutes(resendTtl) + " minute(s) before requesting again"
            );
        }

        // increment request count
        Long count = redisTemplate.opsForValue().increment(reqKey);
        if (count != null && count == 1) {
            redisTemplate.expire(reqKey, Duration.ofSeconds(BLOCK_TTL));
        }
        // block if exceed
        if (count != null && count > MAX_REQUESTS) {
            redisTemplate.opsForValue().set(blockKey, "BLOCKED", Duration.ofSeconds(BLOCK_TTL));
            throw new CustomValidationException(
                    "Too many OTP requests. Blocked for " + (BLOCK_TTL / 60) + " minutes"
            );
        }
        // SET RESEND LOCK (1 min)
        redisTemplate.opsForValue().set(resendKey, "LOCK", Duration.ofSeconds(RESEND_TTL));
    }

    // STORE OTP (invalidate old)
    public void storeOtp(UUID userId, String email, String otp, OtpType type) {
        String id = identifier(userId, email);
        redisTemplate.opsForValue().set(
                key("otp", type, id),
                otp,
                Duration.ofSeconds(OTP_TTL)
        );
    }

    // verify otp
    public String getOtp(UUID userId, String email, OtpType type) {
        String id = identifier(userId, email);
        return redisTemplate.opsForValue().get(key("otp", type, id));
    }

    public void handleInvalidOtp(final UUID userId, final String email, final OtpType type) {

        String id = identifier(userId, email);
        String attemptKey = key("otp:attempt", type, id);
        String blockKey = key("otp:block", type, id);

        Long attempt = redisTemplate.opsForValue().increment(attemptKey);

        if (attempt != null && attempt == 1) {
            redisTemplate.expire(attemptKey, Duration.ofSeconds(BLOCK_TTL));
        }

        if (attempt != null && attempt >= MAX_VERIFY_ATTEMPTS) {
            redisTemplate.opsForValue().set(
                    blockKey,
                    "BLOCKED",
                    Duration.ofSeconds(BLOCK_TTL)
            );
            throw new CustomValidationException(
                    "Too many wrong attempts. Blocked for " + (BLOCK_TTL / 60) + " minutes"
            );
        }
    }

    public void markVerified(UUID userId, String email, OtpType type) {
        String id = identifier(userId, email);

        redisTemplate.opsForValue().set(
                key("otp:verified", type, id),
                "VERIFIED",
                Duration.ofSeconds(VERIFIED_TTL)
        );
    }

    public boolean isVerified(UUID userId, String email, OtpType type) {
        String id = identifier(userId, email);
        String value = redisTemplate.opsForValue().get(key("otp:verified", type, id));
        return "VERIFIED".equals(value);
    }

    public void clearOtpOnly(UUID userId, String email, OtpType type) {

        String id = identifier(userId, email);

        redisTemplate.delete(List.of(
                key("otp", type, id),
                key("otp:attempt", type, id)
        ));
    }

    // clear after success
    public void clearAll(UUID userId, String email, OtpType type) {

        String id = identifier(userId, email);

        redisTemplate.delete(List.of(
                key("otp", type, id),
                key("otp:req", type, id),
                key("otp:resend", type, id),
                key("otp:block", type, id),
                key("otp:attempt", type, id),
                key("otp:verified", type, id)
        ));
    }

}
