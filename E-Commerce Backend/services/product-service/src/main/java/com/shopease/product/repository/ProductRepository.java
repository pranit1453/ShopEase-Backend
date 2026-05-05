package com.shopease.product.repository;

import com.shopease.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsByCategory_CategoryId(UUID categoryCategoryId);

    @Query("""
                    SELECT p.name FROM Product p WHERE p.name = :names
            """)
    Set<String> findExistingNames(Set<String> names);

    Optional<Product> findByProductId(UUID productId);

    @Query("""
                SELECT p
                FROM Product p
                JOIN FETCH p.category c
                WHERE c.categoryId IN :categoryIds
            """)
    List<Product> findByCategoryIdIn(@Param("categoryIds") List<UUID> categoryIds);
}
