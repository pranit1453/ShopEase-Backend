package com.shopease.cart.controller;

import com.shopease.cart.dto.CartToOrderProductRequest;
import com.shopease.cart.dto.OrderResponse;
import com.shopease.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @DeleteMapping("/move-to-wishlist")
    @PreAuthorize("hasRole('USER') and hasAuthority('CART_DELETE_WISHLIST_WRITE')")
    public ResponseEntity<String> movedToWishlist(@RequestParam final @NotNull UUID productId) {
        cartService.movedFromCartToWishlist(productId);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Product with id: " + productId + " has been moved to wishlist");
    }

    @PostMapping("/proceed-to-buy")
    @PreAuthorize("hasRole('USER') and hasAuthority('PRODUCT_BUY')")
    public ResponseEntity<OrderResponse> checkoutCart(@RequestBody @Valid CartToOrderProductRequest request) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(cartService.checkoutCart(request));
    }
}
