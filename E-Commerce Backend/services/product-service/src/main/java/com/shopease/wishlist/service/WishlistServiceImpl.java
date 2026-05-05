package com.shopease.wishlist.service;

import com.shopease.product.entity.Product;
import com.shopease.wishlist.entity.Wishlist;
import com.shopease.wishlist.entity.WishlistItem;
import com.shopease.wishlist.exception.custom.WishlistDuplicateResourceFoundException;
import com.shopease.wishlist.repository.WishlistItemRepository;
import com.shopease.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Override
    @Transactional
    public Wishlist getOrCreateWishlist(final UUID userId) {
        log.info("Fetching wishlist for userId={}", userId);
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Wishlist not found. Creating new wishlist for userId={}", userId);
                    Wishlist wishlist = Wishlist.builder()
                            .userId(userId)
                            .build();
                    Wishlist saved =  wishlistRepository.save(wishlist);
                    log.info("Wishlist created successfully. wishlistId={}, userId={}", saved.getWishlistId(), userId);
                    return saved;
                });
    }

    @Override
    @Transactional
    public WishlistItem addWishlistItem(final Wishlist wishlist, final Product product) {
        log.info("Adding product to wishlist. wishlistId={}, productId={}", wishlist.getWishlistId(), product.getProductId());
        boolean exists = wishlistItemRepository
                .existsByWishlist_WishlistIdAndProductId(wishlist.getWishlistId(), product.getProductId());
        if (exists) {
            log.warn("Attempt to add duplicate product to wishlist blocked. wishlistId={}, productId={}",
                    wishlist.getWishlistId(), product.getProductId());
            throw new WishlistDuplicateResourceFoundException("Product with id " + product.getProductId() + " already exists in Wishlist ");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .productId(product.getProductId())
                .build();

        wishlist.addItem(wishlistItem);
        log.info("Product added to wishlist successfully. wishlistId={}, productId={}",
                wishlist.getWishlistId(), product.getProductId());
        return wishlistItem;
    }

    @Override
    @Transactional
    public void addWishlistItemByProductId(final Wishlist wishlist, final UUID productId) {
        log.info("Adding productId={} to wishlistId={}", productId, wishlist.getWishlistId());
        boolean exists = wishlistItemRepository
                .existsByWishlist_WishlistIdAndProductId(wishlist.getWishlistId(), productId);
        if (exists) {
            log.warn("Duplicate wishlist item detected. wishlistId={}, productId={}",wishlist.getWishlistId(), productId);
            throw new WishlistDuplicateResourceFoundException("Product with id " + productId + " already exists in Wishlist ");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .productId(productId)
                .build();

        wishlist.addItem(wishlistItem);
        log.info("Wishlist item added successfully. wishlistId={}, productId={}",
                wishlist.getWishlistId(), productId);

    }
}
