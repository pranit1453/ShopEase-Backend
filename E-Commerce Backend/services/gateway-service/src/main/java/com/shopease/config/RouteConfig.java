package com.shopease.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r
                        .path("/auth/**")
                        .filters(f -> f.rewritePath(
                                "/auth/(?<segment>.*)",
                                "/${segment}"
                        ))
                        .uri("lb://auth-service")
                )
                .route(r -> r
                        .path("/product/**")
                        .filters(f -> f.rewritePath(
                                "/product/(?<segment>.*)",
                                "/${segment}"
                        ))
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