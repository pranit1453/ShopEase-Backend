package com.shopease.security;

import com.shopease.common.security.helper.SecurityContext;
import com.shopease.product.entity.Product;
import com.shopease.product.exception.ProductResourceNotFoundException;
import com.shopease.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSecurityService {

    private final ProductRepository productRepository;

    @Transactional
    public boolean isProductOwner(final UUID productId) {
        final UUID userId = SecurityContext.getUserIdFromSecurityContext();

        final Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductResourceNotFoundException("Product not found"));

        return product.getCreatedBy().equals(userId);
    }
}
