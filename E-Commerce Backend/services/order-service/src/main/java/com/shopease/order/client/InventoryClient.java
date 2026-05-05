package com.shopease.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service", url = "http://localhost:8082/api/v1/inventory")
public interface InventoryClient {

    @PostMapping("/check")
    Map<UUID, Boolean> checkStock(@RequestBody Map<UUID, Integer> request);

    @PostMapping("/reserve")
    void reserveStock(@RequestBody Map<UUID, Integer> request);

    @PostMapping("/release")
    void releaseStock(@RequestBody Map<UUID, Integer> request);
}
