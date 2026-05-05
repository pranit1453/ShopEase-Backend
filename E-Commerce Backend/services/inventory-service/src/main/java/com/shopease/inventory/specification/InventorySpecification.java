package com.shopease.inventory.specification;

import com.shopease.inventory.entity.Inventory;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {

    public static Specification<Inventory> hasSearch(final Integer quantity) {
        return (root, query, cb) -> {
            if (quantity == null) {
                return cb.conjunction();
            }

            Expression<Integer> available =
                    cb.diff(
                            root.get("totalQuantity"),
                            root.get("reservedQuantity")
                    ).as(Integer.class);

            return cb.or(
                    cb.greaterThanOrEqualTo(root.get("totalQuantity"), quantity),
                    cb.greaterThanOrEqualTo(root.get("reservedQuantity"), quantity),
                    cb.greaterThanOrEqualTo(available, quantity)
            );
        };
    }
}
