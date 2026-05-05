package com.shopease.category.specification;

import com.shopease.category.entity.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CategorySpecification {


    // keyword="SELECT * FROM PRODUCT"
    public static Specification<Category> searchKeyword(final String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                // return cb.conjunction();
                return null;
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("description")), likePattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Category> searchCategory(final UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                // return cb.conjunction();
                return null;
            }
            return cb.equal(root.get("categoryId"), categoryId);
        };
    }

    public static <T> Specification<T> orNullable(Specification<T> s1, Specification<T> s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        return s1.and(s2);
    }

    public static Specification<Category> build(String keyword, UUID categoryId) {
        return orNullable(
                searchKeyword(keyword),
                searchCategory(categoryId)
        );
    }
}

