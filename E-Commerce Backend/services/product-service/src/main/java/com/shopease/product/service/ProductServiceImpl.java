package com.shopease.product.service;

import com.shopease.cart.entity.Cart;
import com.shopease.cart.entity.CartItem;
import com.shopease.cart.service.CartService;
import com.shopease.category.entity.Category;
import com.shopease.category.repository.CategoryRepository;
import com.shopease.client.InventoryClient;
import com.shopease.client.OrderClient;
import com.shopease.common.annotation.LogExecution;
import com.shopease.common.dto.CreateOrderResponse;
import com.shopease.common.dto.ProductDetails;
import com.shopease.common.wrapper.PageResponse;
import com.shopease.product.dto.*;
import com.shopease.product.entity.Product;
import com.shopease.product.enums.ProductSortField;
import com.shopease.product.exception.custom.DuplicateProductFoundException;
import com.shopease.product.exception.custom.InsufficientStockException;
import com.shopease.product.exception.custom.ProductResourceNotFoundException;
import com.shopease.product.mapper.ProductMapper;
import com.shopease.product.repository.ProductRepository;
import com.shopease.product.specification.ProductSpecification;
import com.shopease.wishlist.entity.Wishlist;
import com.shopease.wishlist.entity.WishlistItem;
import com.shopease.wishlist.service.WishlistService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final InventoryClient inventoryClient;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final OrderClient orderClient;

    @Override
    @Transactional
    @LogExecution
    public Set<CreateProductResponse> createProductInBulk(final Set<CreateProductRequest> request) {
        log.info("Bulk product creation started. Total products: {}", request.size());
        Set<UUID> categoryIds = request.stream()
                .map(CreateProductRequest::categoryId)
                .collect(Collectors.toSet());
        log.debug("Fetching categories for IDs: {}", categoryIds);
        Map<UUID, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));
        if (categoryMap.size() != categoryIds.size()) {
            log.warn("Some categories are missing. Expected: {}, Found: {}", categoryIds.size(), categoryMap.size());
        }
        Set<UUID> missingCategories = categoryIds.stream()
                .filter(id -> !categoryMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!missingCategories.isEmpty()) {
            log.error("Missing categories detected: {}", missingCategories);
            throw new ProductResourceNotFoundException("Category not found: " + missingCategories);
        }
        Set<String> seen = new HashSet<>();
        List<Product> products = request.stream()
                .map(req -> {
                    String name = req.name().trim().toLowerCase();
                    String key = name + "_" + req.categoryId();
                    if (!seen.add(key)) {
                        log.error("Duplicate product detected in request: {}", name);
                        throw new DuplicateProductFoundException("Duplicate product in request: " + name);
                    }
                    Product product = productMapper.mappedToProduct(req);
                    product.setCategory(categoryMap.get(req.categoryId()));
                    return product;
                })
                .toList();
        List<Product> savedProducts = productRepository.saveAll(products);
        log.info("Bulk product creation completed. Saved count: {}", savedProducts.size());
        return savedProducts.stream()
                .map(productMapper::mappedToCreateProductResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public CreateProductResponse createProduct(final CreateProductRequest request) {
        log.info("Creating product with categoryId: {}", request.categoryId());
        Category category = categoryRepository.findByCategoryId(request.categoryId())
                .orElseThrow(() -> new ProductResourceNotFoundException("Category not found: " + request.categoryId()));
        Product product = productMapper.mappedToProduct(request);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getProductId());
        return productMapper.mappedToCreateProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public UpdateProductResponse updateProduct(final UUID id, final UpdateProductRequest request) {
        Product product = findByProductId(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());

        Product savedProduct = productRepository.save(product);
        return productMapper.mappedToUpdateProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public UpdateProductResponse patchProduct(UUID productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);
        Product product = findByProductId(productId);
        productMapper.patchProduct(request, product);
        Product savedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", productId);
        return productMapper.mappedToUpdateProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProductById(final UUID productId) {
        log.warn("Deleting product with id: {}", productId);
        Product product = findByProductId(productId);
        Category category = product.getCategory();
        if (category != null) {
            category.removeProduct(product);
        }
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse fetchProductById(final UUID productId) {
        log.debug("Fetching product by id: {}", productId);
        Product product = findByProductId(productId);
        return productMapper.mappedToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> fetchProductsInPage(
            final int page,
            final int size,
            final String keyword,
            final ProductSortField sortBy,
            final String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(Sort.Direction.ASC, sortBy.getField())
                : Sort.by(Sort.Direction.DESC, sortBy.getField());

        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("Fetching products page={}, size={}, keyword={}", page, size, keyword);
        Specification<Product> specification = ProductSpecification.searchKeyword(keyword);
        Page<Product> pages = productRepository.findAll(specification, pageable);
        List<ProductResponse> content = pages.getContent()
                .stream()
                .map(productMapper::mappedToProductResponse)
                .toList();
        log.debug("Fetched {} products", pages.getTotalElements());
        return PageResponse.<ProductResponse>builder()
                .content(content)
                .page(pages.getNumber())
                .size(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    public Boolean checkForProductExistence(final UUID productId) {
        return productRepository.existsById(productId);
    }

    @Override
    @Transactional
    public ProductToCartResponse addProductToCart(final ProductToCartRequest request,
                                                  final UUID userId) {
        log.info("Adding product to cart. productId={}, userId={}, qty={}", request.productId(), userId, request.quantity());
        log.debug("Checking inventory for productId={}", request.productId());
        if (!Boolean.TRUE.equals(inventoryClient.isStockAvailable(request.productId(), request.quantity()))) {
            log.error("Stock not available for productId={}, quantity={}", request.productId(), request.quantity());
            throw new InsufficientStockException("Stock is not available");
        }
        try {
            inventoryClient.reserveStock(request.productId(), request.quantity());
        } catch (FeignException.BadRequest ex) {
            throw new InsufficientStockException("Stock just went out. Try again.");
        }

        Product product = findByProductId(request.productId());
        Cart cart = cartService.getOrCreateCart(userId);

        try {
            CartItem cartItem = cartService.addOrUpdateCartItem(
                    cart, request.productId(), request.quantity()
            );
            log.info("Product added to cart successfully. productId={}, userId={}", request.productId(), userId);
            return ProductToCartResponse.builder()
                    .productId(cartItem.getProductId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(product.getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();

        } catch (Exception ex) {
            inventoryClient.releaseStock(request.productId(), request.quantity());
            throw ex;
        }
    }

    @Override
    @Transactional
    public ProductToWishlistResponse addProductToWishlist(final ProductToWishlistRequest request, final UUID userId) {
        log.info("Adding product to wishlist. productId={}, userId={}", request.productId(), userId);
        Product product = findByProductId(request.productId());
        Wishlist wishlist = wishlistService.getOrCreateWishlist(userId);
        WishlistItem wishlistItem = wishlistService.addWishlistItem(wishlist, product);
        return ProductToWishlistResponse.builder()
                .productId(wishlistItem.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

    @Override
    @Transactional
    public List<ProductResponse> getProductsByIds(final List<UUID> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        return products.stream()
                .map(product -> ProductResponse.builder()
                        .productId(product.getProductId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .description(product.getDescription())
                        .build())
                .toList();
    }

    @Override
    public CreateAndPlaceOrderResponse createAndBuyOrder(UUID productId, int quantity) {
        log.info("Creating order for productId={}, quantity={}", productId, quantity);
        log.debug("Checking stock availability for productId={}", productId);
        if (!Boolean.TRUE.equals(inventoryClient.isStockAvailable(productId, quantity))) {
            throw new InsufficientStockException("Stock is not available");
        }
        try {
            inventoryClient.reserveStock(productId, quantity);
        } catch (FeignException.BadRequest ex) {
            throw new InsufficientStockException("Stock just went out. Try again.");
        }
        try {
            Product product = findByProductId(productId);
            ProductDetails details = ProductDetails.builder()
                    .productId(product.getProductId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();

            CreateOrderResponse response = orderClient.createOrderAndProceedToBuy(details);
            log.info("Order created successfully. productId={}, orderId={}", productId, response.orderId());
            return CreateAndPlaceOrderResponse.builder()
                    .orderId(response.orderId())
                    .message(response.message())
                    .build();
        } catch (Exception ex) {
            log.error("Order failed. Releasing stock for productId={}", productId);
            inventoryClient.releaseStock(productId, quantity);
            throw ex;
        }

    }

    private Product findByProductId(final UUID id) {
        log.debug("Finding product by id: {}", id);
        return productRepository.findByProductId(id)
                .orElseThrow(() ->
                        new ProductResourceNotFoundException("Product with id: " + id + " not found"));
    }
}
