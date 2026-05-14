package com.shopease.inventory.controller;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.inventory.dto.*;
import com.shopease.inventory.enums.InventorySortField;
import com.shopease.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_WRITE')")
    ResponseEntity<CreateInventoryResponse> createInventory(@RequestBody final @Valid CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(inventoryService.createNewInventory(request));
    }

    @GetMapping("/{id}/check")
    @PreAuthorize("hasRole('USER') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Boolean> isStockAvailable(
            @PathVariable final @NotNull UUID id,
            @RequestParam @Min(1) Integer quantity
    ) {
        return ResponseEntity.ok(
                inventoryService.isStockAvailable(id, quantity)
        );
    }

    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Void> reserveStock(
            @PathVariable final @NotNull UUID id,
            @RequestParam @Min(1) Integer quantity
    ) {
        inventoryService.reserveStock(id, quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('USER') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Void> releaseStock(
            @PathVariable final @NotNull UUID id,
            @RequestParam @Min(1) Integer quantity
    ) {
        inventoryService.releaseStock(id, quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<UpdateInventoryResponse> updateInventory(
            @PathVariable final @NotNull UUID id,
            @RequestBody final @Valid UpdateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(inventoryService.updateInventory(id, request));
    }

    @PatchMapping("/patch/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<UpdateInventoryResponse> patchInventory(
            @PathVariable final @NotNull UUID id,
            @RequestBody final @Valid UpdateInventoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(inventoryService.patchInventory(id, request));
    }

    @DeleteMapping("/remove/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_DELETE')")
    public ResponseEntity<String> deleteInventory(@PathVariable final @NotNull UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("Inventory with id: " + id + " has been deleted.");
    }

    @GetMapping("/fetch/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<InventoryResponse> fetchInventoryById(@PathVariable final @NotNull UUID id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(inventoryService.fetchInventoryById(id));
    }

    @GetMapping("/fetch")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<PageResponse<InventoryResponse>> fetchInventoryInPage(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) Integer qty,
            @RequestParam(required = false, defaultValue = "INVENTORY_ID") InventorySortField sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(inventoryService.fetchAllInventoryInPage(page, size, qty, sortBy, sortDirection));
    }

    @PostMapping("/check")
    @PreAuthorize("hasRole('USER') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Map<UUID, Boolean>> checkStockBatch(
            @RequestBody Map<UUID, Integer> request
    ) {
        return ResponseEntity.ok(inventoryService.checkStock(request));
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('USER') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Void> reserveStockBatch(
            @RequestBody Map<UUID, Integer> request
    ) {
        inventoryService.reserveStockBatch(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    @PreAuthorize("hasRole('USER') and hasAuthority('INVENTORY_READ')")
    public ResponseEntity<Void> releaseStockBatch(
            @RequestBody Map<UUID, Integer> request
    ) {
        inventoryService.releaseStockBatch(request);
        return ResponseEntity.ok().build();
    }
}
