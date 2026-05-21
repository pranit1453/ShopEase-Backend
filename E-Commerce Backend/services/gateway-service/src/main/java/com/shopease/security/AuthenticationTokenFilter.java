package com.shopease.security;

import com.shopease.utils.CorrelationIdGenerator;
import com.shopease.utils.TokenUtils;
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

@Slf4j
@Component
@NullMarked
public class AuthenticationTokenFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            // Public Endpoints
            "/auth/api/v1/auth/register",
            "/auth/api/v1/auth/admin",
            "/auth/api/v1/auth/verify-email",
            "/auth/api/v1/auth/resend-otp",
            "/auth/api/v1/auth/login",

            "/product/api/v1/product/fetch/{productId}",
            "/product/api/v1/product/fetch/all",

            "/product/api/v1/category/fetch/{categoryId}",
            "/product/api/v1/category/fetch/all",
            "/product/api/v1/category/fetch/category-with-products",

            // Swagger UI
            "/swagger-ui.html",
            "/swagger-ui/**",

            // OpenAPI Docs
            "/v3/api-docs/**",

            // Microservice Swagger Docs
            "/auth/v3/api-docs/**",
            "/product/v3/api-docs/**",
            "/inventory/v3/api-docs/**",
            "/order/v3/api-docs/**",
            "/payment/v3/api-docs/**",
            "/notification/v3/api-docs/**"

    );
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

        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(endpoint -> pathMatcher.match(endpoint, path));

        if (isPublic) {
            return chain.filter(exchange);
        }

        try {
            String token = TokenUtils.extractToken(exchange);
            if (token == null) {
                return unauthorized(exchange, "Authentication token required");
            }
            accessTokenValidator.validateToken(token);

            String correlationID = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Correlation-ID");

            if (correlationID == null || correlationID.isBlank()) {
                correlationID = CorrelationIdGenerator.generate();
            }

            final String finalCorrelationID = correlationID;
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-Correlation-ID");
                        headers.add("X-Correlation-ID", sanitize(finalCorrelationID));
                    })
                    .build();
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
