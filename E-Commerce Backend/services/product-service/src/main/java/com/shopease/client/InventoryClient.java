package com.shopease.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "inventory-service", url = "http://localhost:8082/api/v1/inventory")
public interface InventoryClient {

    @GetMapping("/{id}/check")
    Boolean isStockAvailable(@PathVariable final @NotNull UUID id,
                             @RequestParam final @NotNull Integer quantity);

    @PostMapping("/{id}/reserve")
    void reserveStock(@PathVariable UUID id,
                      @RequestParam Integer quantity);

    @PostMapping("/{id}/release")
    void releaseStock(@PathVariable UUID id,
                      @RequestParam Integer quantity);
}
