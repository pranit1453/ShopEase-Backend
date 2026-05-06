package com.shopease.cart.service;

import com.shopease.cart.dto.CartToOrderProductRequest;
import com.shopease.cart.dto.OrderRequest;
import com.shopease.cart.dto.OrderRequestItem;
import com.shopease.cart.dto.OrderResponse;
import com.shopease.cart.entity.Cart;
import com.shopease.cart.entity.CartItem;
import com.shopease.cart.exception.CartEmptyException;
import com.shopease.cart.exception.CartNotFoundException;
import com.shopease.cart.exception.ProductNotFoundInCartException;
import com.shopease.cart.repository.CartItemRepository;
import com.shopease.cart.repository.CartRepository;
import com.shopease.client.OrderClient;
import com.shopease.wishlist.entity.Wishlist;
import com.shopease.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistService wishlistService;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public Cart getOrCreateCart(final UUID userId) {
        log.info("Fetching or creating cart for userId: {}", userId);

        return cartRepository.findByUserId(userId)
                .map(cart -> {
                    log.debug("Cart found for userId: {}, cartId: {}", userId, cart.getCartId());
                    return cart;
                })
                .orElseGet(() -> {
                    log.info("No cart found. Creating new cart for userId: {}", userId);
                    Cart cart = Cart.builder()
                            .userId(userId)
                            .build();
                    Cart saved = cartRepository.save(cart);
                    log.info("Cart created with cartId: {} for userId: {}", saved.getCartId(), userId);
                    return saved;
                });
    }

    @Override
    @Transactional
    public CartItem addOrUpdateCartItem(final Cart cart,
                                        final UUID productId,
                                        final Integer quantity) {
        log.info("Adding/updating cart item. cartId: {}, productId: {}, quantity: {}",
                cart.getCartId(), productId, quantity);

        return cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .map(existing -> {
                    log.debug("Product already exists in cart. Updating quantity. Old: {}, Added: {}",
                            existing.getQuantity(), quantity);

                    existing.setQuantity(existing.getQuantity() + quantity);
                    log.info("Updated quantity for productId: {} in cartId: {}", productId, cart.getCartId());
                    return existing;
                })
                .orElseGet(() -> {
                    log.debug("Product not in cart. Creating new cart item");

                    CartItem cartItem = CartItem.builder()
                            .productId(productId)
                            .quantity(quantity)
                            .build();
                    cart.addItem(cartItem);
                    log.info("Added new product to cart. productId: {}, cartId: {}", productId, cart.getCartId());
                    return cartItem;
                });
    }

    @Override
    @Transactional
    public void movedFromCartToWishlist(final UUID productId) {
        log.info("Moving product from cart to wishlist. productId: {}", productId);

        CartItem cartItem = cartItemRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("Product not found in cart. productId: {}", productId);
                    return new ProductNotFoundInCartException("Product with id: " + productId + " not found in cart");
                });
        UUID userId = cartItem.getCart().getUserId();
        UUID cartId = cartItem.getCart().getCartId();
        log.debug("CartItem found. userId: {}, cartId: {}", userId, cartId);

        Wishlist wishlist = wishlistService.getOrCreateWishlist(userId);
        wishlistService.addWishlistItemByProductId(wishlist, cartItem.getProductId());
        cartItemRepository.delete(cartItem);
        log.info("Product moved to wishlist and removed from cart. productId: {}", productId);

        boolean hasItems = cartItemRepository.existsByCart_CartId(cartId);
        if (!hasItems) {
            log.info("Cart is empty. Deleting cartId:: {}", cartId);
            cartRepository.deleteById(cartId);
        }

    }

    @Override
    @Transactional
    public OrderResponse checkoutCart(final CartToOrderProductRequest request) {
        UUID userId = request.userId();
        UUID cartId = request.cartId();
        log.info("Checkout initiated. userId: {}, cartId: {}", userId, cartId);

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, userId)
                .orElseThrow(() -> {
                    log.error("Cart not found. cartId: {}, userId: {}", cartId, userId);
                    return new CartNotFoundException("Cart with id: " + cartId + " not found in cart");
                });

        if (cart.getItems().isEmpty()) {
            log.warn("Checkout failed. Cart is empty. cartId: {}, userId: {}", cartId, userId);

            throw new CartEmptyException("Cart is empty!!!");
        }
        log.debug("Cart has {} items. Preparing order request", cart.getItems().size());

        // building order request
        List<OrderRequestItem> items = cart.getItems()
                .stream()
                .map(item -> OrderRequestItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrderRequest req = OrderRequest.builder()
                .userId(userId)
                .items(items)
                .build();
        log.info("Calling Order Service for checkout. userId: {}", userId);

        OrderResponse response = orderClient.createOrder(req);
        log.info("Order created successfully. orderId: {}, userId: {}",
                response.orderId(), response.userId());
        // clearing cart after successful order
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared after checkout. cartId: {}", cartId);

        return OrderResponse.builder()
                .orderId(response.orderId())
                .userId(response.userId())
                .paymentId(response.paymentId())
                .orderItemResponse(response.orderItemResponse())
                .grandTotal(response.grandTotal())
                .status(response.status())
                .message("Order Created Successfully")
                .build();
    }


}
