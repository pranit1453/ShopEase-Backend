package com.shopease.config;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration is for routing the services url to the api gateway.
 */
@Configuration
public class GatewayConfig {
    @Bean
    public DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator(
            final ReactiveDiscoveryClient discoveryClient,
            final DiscoveryLocatorProperties properties) {
        properties.setLowerCaseServiceId(true);
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }
}
