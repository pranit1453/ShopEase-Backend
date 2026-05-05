package com.shopease.cart.service;

import com.shopease.cart.dto.CartToOrderProductRequest;
import com.shopease.cart.dto.OrderRequest;
import com.shopease.cart.dto.OrderRequestItem;
import com.shopease.cart.dto.OrderResponse;
import com.shopease.cart.entity.Cart;
import com.shopease.cart.entity.CartItem;
import com.shopease.cart.repository.CartItemRepository;
import com.shopease.cart.repository.CartRepository;
import com.shopease.client.OrderClient;
import com.shopease.wishlist.entity.Wishlist;
import com.shopease.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public CartItem addOrUpdateCartItem(final Cart cart,
                                        final UUID productId,
                                        final Integer quantity) {
        return cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return existing;
                })
                .orElseGet(() -> {
                    CartItem cartItem = CartItem.builder()
                            .productId(productId)
                            .quantity(quantity)
                            .build();
                    cart.addItem(cartItem);
                    return cartItem;
                });
    }

    @Override
    @Transactional
    public void movedFromCartToWishlist(final UUID productId) {
        CartItem cartItem = cartItemRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("Product with id: " + productId + " not found in cart"));
        UUID userId = cartItem.getCart().getUserId();
        UUID cartId = cartItem.getCart().getCartId();
        Wishlist wishlist = wishlistService.getOrCreateWishlist(userId);
        wishlistService.addWishlistItemByProductId(wishlist, cartItem.getProductId());
        cartItemRepository.delete(cartItem);
        boolean hasItems = cartItemRepository.existsByCart_CartId(cartId);
        if (!hasItems) {
            cartRepository.deleteById(cartId);
        }

    }

    @Override
    @Transactional
    public OrderResponse checkoutCart(final CartToOrderProductRequest request) {
        UUID userId = request.userId();
        UUID cartId = request.cartId();

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, userId)
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("Cart with id: " + cartId + " not found in cart"));

        if (cart.getItems().isEmpty()) {
            throw new CustomResourceNotFoundException("Cart is empty!!!");
        }

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

        OrderResponse response = orderClient.createOrder(req);

        // clearing cart after successful order
        cart.getItems().clear();
        cartRepository.save(cart);

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
