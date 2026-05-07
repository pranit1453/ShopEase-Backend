package com.shopease.product.service;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.product.dto.*;
import com.shopease.product.enums.ProductSortField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;
import java.util.Set;
import java.util.UUID;
public interface ProductService {
    Set<CreateProductResponse> createProductInBulk(Set<@Valid CreateProductRequest> request);

    CreateProductResponse createProduct(@Valid CreateProductRequest request);

    UpdateProductResponse updateProduct(@NotNull UUID productId, @Valid UpdateProductRequest request);

    UpdateProductResponse patchProduct(@NotNull UUID productId, @Valid UpdateProductRequest request);

    void deleteProductById(@NotNull UUID productId);

    ProductResponse fetchProductById(@NotNull UUID productId);

    PageResponse<ProductResponse> fetchProductsInPage(int page, int size, String keyword, ProductSortField sortBy, String sortDirection);

    Boolean checkForProductExistence(@NotNull UUID productId);

    ProductToCartResponse addProductToCart(@Valid ProductToCartRequest request, UUID userId);

    ProductToWishlistResponse addProductToWishlist(@Valid ProductToWishlistRequest request, UUID userId);

    List<ProductResponse> getProductsByIds(@Valid List<UUID> productIds);

    CreateAndPlaceOrderResponse createAndBuyOrder(@NotNull UUID productId, int quantity);
}
