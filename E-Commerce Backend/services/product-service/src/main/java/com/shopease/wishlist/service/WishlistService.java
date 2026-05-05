package com.shopease.wishlist.service;

import com.shopease.product.entity.Product;
import com.shopease.wishlist.entity.Wishlist;
import com.shopease.wishlist.entity.WishlistItem;

import java.util.UUID;

public interface WishlistService {
    Wishlist getOrCreateWishlist(UUID userId);

    WishlistItem addWishlistItem(Wishlist wishlist, Product product);

    void addWishlistItemByProductId(Wishlist wishlist, UUID productId);
}
