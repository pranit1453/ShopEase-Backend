package com.shopease.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Configuration
public class RouteConfig {

    private final RedisRateLimiter redisRateLimiter;
    private final KeyResolver userKeyResolver;

    public RouteConfig(RedisRateLimiter redisRateLimiter,
                              KeyResolver userKeyResolver) {
        this.redisRateLimiter = redisRateLimiter;
        this.userKeyResolver = userKeyResolver;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r
                        .path("/auth/**")
                        .filters(f -> f.rewritePath(
                                "/auth/(?<segment>.*)",
                                "/${segment}")
                                .requestRateLimiter(config->config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                )
                        )
                        .uri("lb://auth-service")
                )
                .route(r -> r
                        .path("/product/**")
                        .filters(f -> f.rewritePath(
                                "/product/(?<segment>.*)",
                                "/${segment}")
                                .requestRateLimiter(config->config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                                // true -> multiply by previous delay
                                                // false -> multiply by initial delay
                                        ))
                                .circuitBreaker(circuitBreakerConfig->circuitBreakerConfig
                                        .setName("circuitBreakerProduct")
                                        .setFallbackUri("forward:/circuitBreakerProduct/fallback"))

                        )
                        .metadata("response-timeout", 2000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://product-service")
                )
                .route(r -> r
                        .path("/inventory/**")
                        .filters(f -> f.rewritePath(
                                "/inventory/(?<segment>.*)",
                                "/${segment}"
                        ))
                        .uri("lb://inventory-service")
                )
                .route(r -> r
                        .path("/order/**")
                        .filters(f -> f.rewritePath(
                                "/order/(?<segment>.*)",
                                "/${segment}"
                        ))
                        .uri("lb://order-service")
                )
                .route(r -> r
                        .path("/notification/**")
                        .filters(f -> f.rewritePath(
                                "/notification/(?<segment>.*)",
                                "/${segment}"
                        ))
                        .uri("lb://notification-service")
                )
                .route(r -> r
                        .path("/payment/**")
                        .filters(f -> f.rewritePath(
                                "/payment/(?<segment>.*)",
                                "/${segment}"
                        ))
                        .uri("lb://payment-service")
                )
                .build();
    }
    
}
/*
    Customized Service Routing
*/