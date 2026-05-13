package com.shopease.permission.specification;

import com.shopease.permission.entity.Permission;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PermissionSpecification {

    public static Specification<Permission> searchKeyword(final String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("permissionCode")), likePattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
