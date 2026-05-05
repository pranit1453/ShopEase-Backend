package com.shopease.product.specification;

import com.shopease.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class ProductSpecification {

    public static Specification<Product> searchKeyword(final String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("description")), likePattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
