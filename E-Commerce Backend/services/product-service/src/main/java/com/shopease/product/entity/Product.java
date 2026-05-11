package com.shopease.product.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.shopease.auditing.entity.AuditEntity;
import com.shopease.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "product",
        schema = "product_schema",
        indexes = {
                @Index(name = "idx_product_name", columnList = "product_name"),
                @Index(name = "idx_product_category", columnList = "category_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_name_category",
                        columnNames = {"product_name", "category_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "product_description", length = 1000)
    private String description;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category category;
}
