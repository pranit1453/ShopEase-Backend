package com.shopease.cart.service;

import com.shopease.cart.dto.CartToOrderProductRequest;
import com.shopease.cart.dto.OrderResponse;
import com.shopease.cart.entity.Cart;
import com.shopease.cart.entity.CartItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.UUID;

public interface CartService {
    Cart getOrCreateCart(UUID userId);

    CartItem addOrUpdateCartItem(Cart cart, UUID productId, @Min(1) Integer quantity);

    void movedFromCartToWishlist(@NotNull UUID productId);

    OrderResponse checkoutCart(@Valid CartToOrderProductRequest request);
}
