package com.shopease.cart.repository;

import com.shopease.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);

    Optional<Cart> findByCartIdAndUserId(UUID cartId, UUID userId);
}
