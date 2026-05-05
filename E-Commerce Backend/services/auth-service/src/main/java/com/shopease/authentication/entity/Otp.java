package com.shopease.authentication.entity;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Otp {

    private UUID otpId;
    private String otp;
    private UUID userId;
    private String email;
    @Builder.Default
    private boolean isOtpUsed = false;
    private Instant issuedAt;
    private Instant expiresAt;
}
