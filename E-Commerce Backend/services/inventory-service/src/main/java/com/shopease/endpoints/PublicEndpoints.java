package com.shopease.endpoints;

import com.shopease.common.security.endpoints.PublicEndpointProvider;
import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints implements PublicEndpointProvider {
    @Override
    public String[] publicEndpoints() {
        return new String[]{
        };
    }
}
