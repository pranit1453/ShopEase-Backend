package com.shopease.product.controller;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.product.dto.*;
import com.shopease.product.enums.ProductSortField;
import com.shopease.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping("/list/create")
    public ResponseEntity<Set<CreateProductResponse>> createProductsInBulk(@RequestBody final Set<@Valid CreateProductRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProductInBulk(request));
    }

    @PostMapping("/create")
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody final @Valid CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UpdateProductResponse> updateProduct(@PathVariable final @NotNull UUID id,
                                                               @RequestBody @Valid UpdateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.updateProduct(id, request));
    }

    @PatchMapping("/patch/{id}")
    public ResponseEntity<UpdateProductResponse> patchProduct(@PathVariable final @NotNull UUID id,
                                                              @RequestBody @Valid UpdateProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.patchProduct(id, request));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable final @NotNull UUID id) {
        productService.deleteProductById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("Product with id: " + id + " has been deleted");
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<ProductResponse> fetchProductById(@PathVariable final @NotNull UUID id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.fetchProductById(id));
    }

    @GetMapping("/fetch/all")
    public ResponseEntity<PageResponse<ProductResponse>> fetchProductsInPage(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "PRODUCT_ID") ProductSortField sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.fetchProductsInPage(page, size, keyword, sortBy, sortDirection));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> productExists(@PathVariable final @NotNull UUID id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.checkForProductExistence(id));
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<ProductToCartResponse> addToCart(@RequestBody final @Valid ProductToCartRequest request) {
        UUID userId = UUID.fromString("95d6810a-1ca5-4e75-9b9d-a792c8d07395");

        System.out.println(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.addProductToCart(request, userId));
    }

    @PostMapping("/add-to-wishlist")
    public ResponseEntity<ProductToWishlistResponse> addToWishlist(@RequestBody final @Valid ProductToWishlistRequest request) {
        UUID userId = UUID.fromString("3a6bea85-1f4e-4a77-b3d6-13ab65f06589");
        System.out.println(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.addProductToWishlist(request, userId));
    }

    @PostMapping("/batch")
    public List<ProductResponse> getProductsByIds(@RequestBody final @Valid List<UUID> productIds) {
        return productService.getProductsByIds(productIds);
    }

    @PostMapping("/purchase/{productId}")
    public ResponseEntity<CreateAndPlaceOrderResponse> createAndBuyOrder(
            @PathVariable final @NotNull UUID productId,
            @RequestParam int quantity) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createAndBuyOrder(productId, quantity));
    }
}
