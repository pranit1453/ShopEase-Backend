package com.shopease.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class TokenUtils {

    // In Production Keep Only One Header Or Cookie
    public static String extractToken(ServerWebExchange exchange) {
        // Check authorization header

        log.info("Checking cookies...........");
        // Check in cookies
        var cookies = exchange.getRequest().getCookies();
        var cookie = cookies.getFirst("access_token");
        log.info("Cookie found: {}", cookie);
        if (cookie != null && !cookie.getValue().isBlank()) {
            log.info("Cookie found: {}", cookie.getValue());
            return cookie.getValue();
        }

        log.info("Authorization header checking.......");
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("Authorization header found: {}", header);
        if(header != null && header.startsWith("Bearer ")){
            log.info("Authorization header found: {}", header);
            return header.substring(7);
        }
        return null;
    }
}
