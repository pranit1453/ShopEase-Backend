package com.shopease.wishlist.repository;

import com.shopease.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    Optional<WishlistItem> findByWishlist_WishlistIdAndProductId(UUID wishlistWishlistId, UUID productId);

    boolean existsByWishlist_WishlistIdAndProductId(UUID wishlistId, UUID productId);
}
