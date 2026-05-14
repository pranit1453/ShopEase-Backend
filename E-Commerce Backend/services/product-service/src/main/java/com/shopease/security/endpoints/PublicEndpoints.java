package com.shopease.security.endpoints;

import com.shopease.common.security.endpoints.PublicEndpointProvider;
import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints implements PublicEndpointProvider {
    @Override
    public String[] publicEndpoints() {
        return new String[]{
                "/api/v1/product/fetch/{productId}",
                "/api/v1/product/fetch/all",
                "/api/v1/category/fetch/all",
                "/api/v1/category/fetch/{categoryId}",
                "/api/v1/category/fetch/category-with-products"
        };
    }
}
