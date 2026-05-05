package com.shopease.inventory.entity;

import com.shopease.exception.custom.InvalidResourceException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "inventories", schema = "inventory_schema",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_product", columnNames = "productId")
        },
        indexes = {
                @Index(name = "idx_inventory_product", columnList = "productId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID inventoryId;

    @Column(nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;


    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }


    public void addStock(int qty) {
        validate(qty);
        this.totalQuantity += qty;
    }

    public void reserve(int qty) {
        validate(qty);
        if (getAvailableQuantity() < qty) {
            throw new InvalidResourceException("Insufficient stock");
        }
        this.reservedQuantity += qty;
    }

    public void release(int qty) {
        validate(qty);
        if (reservedQuantity < qty) {
            throw new InvalidResourceException("Invalid release");
        }
        this.reservedQuantity -= qty;
    }

    public void deduct(int qty) {
        validate(qty);
        if (reservedQuantity < qty) {
            throw new InvalidResourceException("Invalid deduction");
        }
        this.reservedQuantity -= qty;
        this.totalQuantity -= qty;
    }

    private void validate(int qty) {
        if (qty <= 0) {
            throw new InvalidResourceException("Invalid quantity");
        }
    }
}