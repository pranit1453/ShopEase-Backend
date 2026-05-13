package com.shopease.security;

import com.shopease.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@NullMarked
public class AuthenticationTokenFilter implements GlobalFilter, Ordered {

    private final AccessTokenValidator accessTokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationTokenFilter(AccessTokenValidator accessTokenValidator) {
        this.accessTokenValidator = accessTokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // allow auth endpoints
        if (pathMatcher.match("/auth/api/v1/**", path)) {
            return chain.filter(exchange);
        }

        try {
            String token = TokenUtils.extractToken(exchange);
            log.debug("Token: {}", token);
            if (token == null) {
                return unauthorized(exchange, "Authentication token required");
            }
            Claims claims = accessTokenValidator.validateToken(token);
            log.debug("Claims: {}", claims);
            String userId = claims.getSubject();
            Object roleObj = claims.get("role");

            List<String> roles = roleObj == null
                    ? List.of()
                    : ((List<?>) roleObj)
                      .stream()
                      .map(Object::toString)
                      .toList();
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", sanitize(userId))
                    .header("X-User-Roles",
                            roles.stream()
                                    .map(r -> "ROLE_" + r)
                                    .collect(Collectors.joining(","))).build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)
        ));
    }

    // runs early
    @Override
    public int getOrder() {
        return -1;
    }


    private String sanitize(String value) {
        return value.replaceAll("[\\n\\r\\t]", "");
    }
}
