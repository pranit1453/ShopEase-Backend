package com.shopease.inventory.service;

import com.shopease.client.ProductClient;
import com.shopease.exception.custom.ResourceNotFoundException;
import com.shopease.inventory.dto.*;
import com.shopease.inventory.entity.Inventory;
import com.shopease.inventory.enums.InventorySortField;
import com.shopease.inventory.mapper.InventoryMapper;
import com.shopease.inventory.repository.InventoryRepository;
import com.shopease.inventory.specification.InventorySpecification;
import com.shopease.common.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Service
@RequiredArgsConstructor
@Validated
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public CreateInventoryResponse createNewInventory(final CreateInventoryRequest request) {
        if (!Boolean.TRUE.equals(productClient.isProductExists(request.productId()))) {
            throw new ResourceNotFoundException(
                    "Product with id " + request.productId() + " not found"
            );
        }
        Inventory inventory = inventoryMapper.mappedToInventory(request);
        inventory.setTotalQuantity(
                Optional.ofNullable(inventory.getTotalQuantity()).orElse(0));
        inventory.setReservedQuantity(
                Optional.ofNullable(inventory.getReservedQuantity()).orElse(0));

        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.mappedToCreateInventoryResponse(savedInventory);
    }

    @Override
    @Transactional
    public Boolean isStockAvailable(final UUID id, final Integer quantity) {
        Inventory inventory = findByProductId(id);
        return inventory.getAvailableQuantity() >= quantity;
    }

    @Override
    @Transactional
    public void reserveStock(final UUID id, final Integer quantity) {
        Inventory inventory = findByProductId(id);
        inventory.reserve(quantity);
    }

    @Override
    @Transactional
    public void releaseStock(final UUID id, final Integer quantity) {
        Inventory inventory = findByProductId(id);
        inventory.release(quantity);
    }

    @Override
    @Transactional
    public UpdateInventoryResponse updateInventory(final UUID id, final UpdateInventoryRequest request) {
        Inventory inventory = findByInventoryId(id);

        inventory.setTotalQuantity(
                Optional.ofNullable(request.totalQuantity()).orElse(inventory.getTotalQuantity()));
        inventory.setReservedQuantity(
                Optional.ofNullable(request.reservedQuantity()).orElse(inventory.getReservedQuantity()));

        return UpdateInventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId())
                .totalQuantity(inventory.getTotalQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }

    @Override
    @Transactional
    public UpdateInventoryResponse patchInventory(final UUID id, final UpdateInventoryRequest request) {
        Inventory inventory = findByInventoryId(id);
        inventoryMapper.patchInventory(request, inventory);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return UpdateInventoryResponse.builder()
                .inventoryId(savedInventory.getInventoryId())
                .productId(savedInventory.getProductId())
                .totalQuantity(savedInventory.getTotalQuantity())
                .reservedQuantity(savedInventory.getReservedQuantity())
                .build();
    }

    @Override
    @Transactional
    public void deleteInventory(final UUID id) {
        Inventory inventory = findByInventoryId(id);
        inventoryRepository.deleteById(inventory.getInventoryId());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse fetchInventoryById(final UUID id) {
        return inventoryRepository.findInventoryById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> fetchAllInventoryInPage(
            int page,
            int size,
            Integer qty,
            InventorySortField sortBy,
            String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(Sort.Direction.ASC, sortBy.getField())
                : Sort.by(Sort.Direction.DESC, sortBy.getField());

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Inventory> specification = InventorySpecification.hasSearch(qty);
        Page<Inventory> pages = inventoryRepository.findAll(specification, pageable);
        List<InventoryResponse> content = pages.getContent()
                .stream()
                .map(mapper -> {
                    return InventoryResponse.builder()
                            .inventoryId(mapper.getInventoryId())
                            .productId(mapper.getProductId())
                            .totalQuantity(mapper.getTotalQuantity())
                            .reservedQuantity(mapper.getReservedQuantity())
                            .availableQuantity(mapper.getAvailableQuantity())
                            .build();
                }).toList();
        return PageResponse.<InventoryResponse>builder()
                .content(content)
                .page(pages.getNumber())
                .size(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    @Transactional
    public Map<UUID, Boolean> checkStock(Map<UUID, Integer> request) {
        Map<UUID, Boolean> result = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : request.entrySet()) {
            Inventory inventory = findByProductId(entry.getKey());
            result.put(entry.getKey(), inventory.getAvailableQuantity() >= inventory.getReservedQuantity());
        }
        return result;
    }

    @Override
    @Transactional
    public void reserveStockBatch(Map<UUID, Integer> request) {

        for (Map.Entry<UUID, Integer> entry : request.entrySet()) {
            Inventory inventory = findByProductId(entry.getKey());
            inventory.reserve(entry.getValue());
        }
    }

    @Override
    @Transactional
    public void releaseStockBatch(Map<UUID, Integer> request) {
        for (Map.Entry<UUID, Integer> entry : request.entrySet()) {
            Inventory inventory = findByProductId(entry.getKey());
            inventory.release(entry.getValue());
        }
    }

    private Inventory findByProductId(final UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found in Inventory"));
    }

    private Inventory findByInventoryId(UUID inventoryId) {
        return inventoryRepository.findByInventoryId(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory with id " + inventoryId + " not found"));
    }
}
