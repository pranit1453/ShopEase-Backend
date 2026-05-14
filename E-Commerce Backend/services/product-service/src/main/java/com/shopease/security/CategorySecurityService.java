package com.shopease.security;

import com.shopease.category.entity.Category;
import com.shopease.category.exception.CategoryNotFoundException;
import com.shopease.category.repository.CategoryRepository;
import com.shopease.common.security.helper.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorySecurityService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public boolean isCategoryOwner(final UUID categoryId) {
        final UUID userId = SecurityContext.getUserIdFromSecurityContext();
        final Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Category not found with id " + categoryId));
        return category.getCreatedBy().equals(userId);
    }
}
