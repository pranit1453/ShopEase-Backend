package com.shopease.category.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shopease.auditing.entity.AuditEntity;
import com.shopease.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "category",
        schema = "category_schema",
        indexes = {
                @Index(name = "idx_category_name", columnList = "category_name")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_name",
                        columnNames = "category_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID categoryId;

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(name = "category_description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    @JsonManagedReference
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }
}
