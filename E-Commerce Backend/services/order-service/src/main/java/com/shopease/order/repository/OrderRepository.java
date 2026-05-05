package com.shopease.order.repository;

import com.shopease.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByUserId(UUID userId);

    List<Order> findByOrderId(UUID orderId);

    @Query("""
                SELECT o
                FROM Order o
                WHERE o.orderId = :orderId
            """)
    Optional<Order> findOrderByOrderId(@Param("orderId") UUID orderId);

    @Query("""
              select o from Order o
              left join fetch o.items
              where o.orderId = :orderId
            """)
    Optional<Order> findOrderByOrderIdWithItems(UUID orderId);
}
