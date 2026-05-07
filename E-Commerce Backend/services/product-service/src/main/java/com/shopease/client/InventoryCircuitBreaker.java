package com.shopease.client;

import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
public class InventoryCircuitBreaker implements InventoryClient{
    @Override
    public Boolean isStockAvailable(UUID id, Integer quantity) {
        System.out.print("Inventory Circuit Breaker Stock Availability");
        return null;
    }

    @Override
    public void reserveStock(UUID id, Integer quantity) {
        System.out.print("Inventory Circuit Breaker Stock Reserve");
    }

    @Override
    public void releaseStock(UUID id, Integer quantity) {
        System.out.print("Inventory Circuit Breaker Stock release");
    }
}
