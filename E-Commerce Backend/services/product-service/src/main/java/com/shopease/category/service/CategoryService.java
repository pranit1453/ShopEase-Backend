package com.shopease.category.service;

import com.shopease.category.dto.*;
import com.shopease.category.enums.CategorySortField;
import com.shopease.common.wrapper.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public interface CategoryService {
    Set<CreateCategoryResponse> createCategoriesInBulk(@NotEmpty Set<@Valid CreateCategoryRequest> request);

    CreateCategoryResponse createCategory(@Valid CreateCategoryRequest request);

    UpdateCategoryResponse updateCategoryById(@NotNull UUID id, @Valid UpdateCategoryRequest request);

    UpdateCategoryResponse patchCategoryById(@NotNull UUID id, @Valid UpdateCategoryRequest request);

    void deleteCategoryById(@NotNull UUID id);

    CategoryResponse fetchCategoryById(@NotNull UUID id);

    PageResponse<CategoryResponse> fetchCategoriesInPage(int page, int size, String keyword, CategorySortField sortBy, String sortDirection);

    PageResponse<CategoryWithProductResponse> fetchCategoriesWithProductsInPages(int page, int size, String keyword, UUID categoryId, CategorySortField sortBy, String sortDirection);
}
