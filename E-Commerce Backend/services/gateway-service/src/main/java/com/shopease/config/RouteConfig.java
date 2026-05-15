package com.shopease.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.Duration;

/**
 * This configuration is for the customize routing with api gateway with filter, circuit breaker, load balancer
 * Spring Cloud LoadBalancer ---> discover service instances dynamically
 */
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
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(circuitBreakerConfig -> circuitBreakerConfig
                                        .setName("circuitBreakerAuth")
                                        .setFallbackUri("forward:/circuitBreakerAuth/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100), // first backoff
                                                Duration.ofMillis(800), // max backoff
                                                2,
                                                true
                                        ))

                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://auth-service")
                )
                .route(r -> r
                        .path("/product/**")
                        .filters(f -> f.rewritePath(
                                        "/product/(?<segment>.*)",
                                        "/${segment}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(circuitBreakerConfig -> circuitBreakerConfig
                                        .setName("circuitBreakerProduct")
                                        .setFallbackUri("forward:/circuitBreakerProduct/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                        ))
                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://product-service")
                )
                .route(r -> r
                        .path("/inventory/**")
                        .filters(f -> f.rewritePath(
                                        "/inventory/(?<segment>.*)",
                                        "/${segment}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("circuitBreakerInventory")
                                        .setFallbackUri("forward:/circuitBreakerInventory/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                        ))
                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://inventory-service")
                )
                .route(r -> r
                        .path("/order/**")
                        .filters(f -> f.rewritePath(
                                        "/order/(?<segment>.*)",
                                        "/${segment}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("circuitBreakerOrder")
                                        .setFallbackUri("forward:/circuitBreakerOrder/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                        ))
                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://order-service")
                )
                .route(r -> r
                        .path("/notification/**")
                        .filters(f -> f.rewritePath(
                                        "/notification/(?<segment>.*)",
                                        "/${segment}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("circuitBreakerNotification")
                                        .setFallbackUri("forward:/circuitBreakerNotification/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                        ))
                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://notification-service")
                )
                .route(r -> r
                        .path("/payment/**")
                        .filters(f -> f.rewritePath(
                                        "/payment/(?<segment>.*)",
                                        "/${segment}")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(userKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .circuitBreaker(config -> config
                                        .setName("circuitBreakerPayment")
                                        .setFallbackUri("forward:/circuitBreakerPayment/fallback"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .setBackoff(
                                                Duration.ofMillis(100),
                                                Duration.ofMillis(800),
                                                2,
                                                true
                                        ))
                        )
                        .metadata("response-timeout", 8000)
                        .metadata("connect-timeout", 5000)
                        .uri("lb://payment-service")
                )
                .build();
    }

}
