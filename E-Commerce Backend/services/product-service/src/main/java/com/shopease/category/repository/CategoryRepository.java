package com.shopease.category.repository;

import com.shopease.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    @Query("""
            SELECT c.name FROM Category c WHERE c.name IN :names
            """)
    Set<String> findExistingNames(Set<String> names);

    boolean existsByName(String name);

    Optional<Category> findByCategoryId(UUID categoryId);

}
