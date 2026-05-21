package com.shopease.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class CorrelationIdGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMddHHmm");

    private CorrelationIdGenerator() {
    }

    public static String generate() {
        
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .format(FORMATTER);

        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return "TRACE-" + timestamp + "-" + random;
    }
}
