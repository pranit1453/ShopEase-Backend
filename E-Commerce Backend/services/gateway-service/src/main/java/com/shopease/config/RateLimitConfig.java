package com.shopease.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * This configuration is for the Rate limiting in request using spring cloud.
 * RedisRateLimiter class
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress == null) {
                return Mono.just("Unknown");
            }
            return Mono.just(remoteAddress.getAddress().getHostAddress());
        };
    }

    @Bean
    public RedisRateLimiter
    redisRateLimiter() {
        return new RedisRateLimiter(10, //20 requests/sec allowed
                20, //can handle spikes up to 40
                1 //each request consumes 1 token
        );
    }
}
