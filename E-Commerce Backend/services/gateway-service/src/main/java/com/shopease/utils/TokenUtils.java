package com.shopease.utils;

import org.springframework.web.server.ServerWebExchange;


public class TokenUtils {

    // In Production Keep Only One Header Or Cookie
    public static String extractToken(ServerWebExchange exchange) {
        // Check authorization header
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if(header != null && header.startsWith("Bearer ")){
            return header.substring(7);
        }

        // Check in cookies
        var cookies = exchange.getRequest().getCookies();
        var cookie = cookies.getFirst("access_token");

        if (cookie != null && !cookie.getValue().isBlank()) {
            return cookie.getValue();
        }

        return null;
    }
}
