package com.shopease.product.controller;

import com.shopease.common.security.helper.SecurityContext;
import com.shopease.common.wrapper.PageResponse;
import com.shopease.product.dto.*;
import com.shopease.product.enums.ProductSortField;
import com.shopease.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping("/list/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<Set<CreateProductResponse>> createProductsInBulk(@RequestBody final Set<@Valid CreateProductRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProductInBulk(request));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody final @Valid CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_WRITE') and @productSecurityService.isProductOwner(#productId)")
    public ResponseEntity<UpdateProductResponse> updateProduct(@PathVariable final @NotNull UUID productId,
                                                               @RequestBody @Valid UpdateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.updateProduct(productId, request));
    }

    @PatchMapping("/patch/{productId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_WRITE') and @productSecurityService.isProductOwner(#productId)")
    public ResponseEntity<UpdateProductResponse> patchProduct(@PathVariable final @NotNull UUID productId,
                                                              @RequestBody @Valid UpdateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.patchProduct(productId, request));
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_DELETE') and @productSecurityService.isProductOwner(#productId)")
    public ResponseEntity<String> deleteProduct(@PathVariable final @NotNull UUID productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("Product with productId: " + productId + " has been deleted");
    }

    @GetMapping("/fetch/{productId}")
    public ResponseEntity<ProductResponse> fetchProductById(@PathVariable final @NotNull UUID productId) {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        log.debug("UserID in Security Context: {} for searching product with ProductID: {}", userId, productId);
        ProductResponse response = productService.fetchProductById(productId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/fetch/all")
    public ResponseEntity<PageResponse<ProductResponse>> fetchProductsInPage(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "PRODUCT_ID") ProductSortField sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        log.debug("UserID in Security Context: {} for searching products", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.fetchProductsInPage(page, size, keyword, sortBy, sortDirection));
    }

    @GetMapping("/{productId}/exists")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Boolean> productExists(@PathVariable final @NotNull UUID productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.checkForProductExistence(productId));
    }

    @PostMapping("/add-to-cart")
    @PreAuthorize("hasRole('USER') and hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductToCartResponse> addToCart(@RequestBody final @Valid ProductToCartRequest request) {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        log.debug("UserID in Security Context for adding to cart: {}", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.addProductToCart(request, userId));
    }

    @PostMapping("/add-to-wishlist")
    @PreAuthorize("hasRole('USER') and hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ProductToWishlistResponse> addToWishlist(@RequestBody final @Valid ProductToWishlistRequest request) {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        log.debug("UserID in Security Context for adding to wishlist: {}", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.addProductToWishlist(request, userId));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('USER') and hasAuthority('PRODUCT_READ')")
    public List<ProductResponse> getProductsByIds(@RequestBody final @Valid List<UUID> productIds) {
        return productService.getProductsByIds(productIds);
    }

    @PostMapping("/purchase/{productId}")
    @PreAuthorize("hasRole('USER') and hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<CreateAndPlaceOrderResponse> createAndBuyOrder(
            @PathVariable final @NotNull UUID productId,
            @RequestParam int quantity) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createAndBuyOrder(productId, quantity));
    }
}
