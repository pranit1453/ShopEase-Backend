package com.shopease.inventory.service;

import com.shopease.common.inventory.dto.*;
import com.shopease.inventory.dto.*;
import com.shopease.inventory.enums.InventorySortField;
import com.shopease.common.wrapper.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public interface InventoryService {
    CreateInventoryResponse createNewInventory(@Valid CreateInventoryRequest request);

    Boolean isStockAvailable(@NotNull UUID id, @Min(1) Integer quantity);

    void reserveStock(@NotNull UUID id, @Min(1) Integer quantity);

    void releaseStock(@NotNull UUID id, @Min(1) Integer quantity);

    UpdateInventoryResponse updateInventory(@NotNull UUID id, @Valid UpdateInventoryRequest request);

    UpdateInventoryResponse patchInventory(@NotNull UUID id, @Valid UpdateInventoryRequest request);

    void deleteInventory(@NotNull UUID id);

    InventoryResponse fetchInventoryById(@NotNull UUID id);

    PageResponse<InventoryResponse> fetchAllInventoryInPage(int page, int size, Integer qty, InventorySortField sortBy, String sortDirection);

    Map<UUID, Boolean> checkStock(Map<UUID, Integer> request);

    void reserveStockBatch(Map<UUID, Integer> request);

    void releaseStockBatch(Map<UUID, Integer> request);
}
