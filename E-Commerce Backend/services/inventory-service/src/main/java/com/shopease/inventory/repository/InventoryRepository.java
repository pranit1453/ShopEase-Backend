package com.shopease.inventory.repository;

import com.shopease.inventory.dto.InventoryResponse;
import com.shopease.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID>, JpaSpecificationExecutor<Inventory> {
    boolean existsByProductId(UUID uuid);

    Optional<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findByInventoryId(UUID inventoryId);

    @Query("""
                SELECT new com.shopease.inventory.dto.InventoryResponse(
                    i.inventoryId,
                    i.productId,
                    i.totalQuantity,
                    i.reservedQuantity,
                    (i.totalQuantity - i.reservedQuantity)
                )
                FROM Inventory i
                WHERE i.inventoryId = :id
            """)
    Optional<InventoryResponse> findInventoryById(UUID id);
}
