package com.shopease.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", url = "http://localhost:8083/api/v1/product")
@Validated
public interface ProductClient {
    @GetMapping("/{id}/exists")
    Boolean isProductExists(@PathVariable @NotNull final UUID id);

}
