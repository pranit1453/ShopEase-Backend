package com.shopease.category.controller;

import com.shopease.category.dto.*;
import com.shopease.category.enums.CategorySortField;
import com.shopease.category.service.CategoryService;
import com.shopease.common.wrapper.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/list/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<Set<CreateCategoryResponse>> listOfCreateCategories(@RequestBody @NotEmpty final Set<@Valid CreateCategoryRequest> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategoriesInBulk(request));

    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<CreateCategoryResponse> createCategory(@RequestBody final @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @PutMapping("/update/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CATEGORY_WRITE') and @categorySecurityService.isCategoryOwner(#categoryId)")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(@PathVariable @NotNull final UUID categoryId,
                                                                 @RequestBody @Valid final UpdateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.updateCategoryById(categoryId, request));
    }

    @PatchMapping("/patch/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CATEGORY_WRITE') and @categorySecurityService.isCategoryOwner(#categoryId)")
    public ResponseEntity<UpdateCategoryResponse> patchCategory(@PathVariable @NotNull final UUID categoryId,
                                                                @RequestBody @Valid final UpdateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.patchCategoryById(categoryId, request));
    }

    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CATEGORY_DELETE') and @categorySecurityService.isCategoryOwner(#categoryId)")
    public ResponseEntity<String> deleteCategory(@PathVariable @NotNull final UUID categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("Category with categoryId: " + categoryId + " was deleted successfully");
    }

    @GetMapping("/fetch/{categoryId}")
    public ResponseEntity<CategoryResponse> fetchCategory(@PathVariable @NotNull final UUID categoryId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.fetchCategoryById(categoryId));
    }

    @GetMapping("/fetch/all")
    public ResponseEntity<PageResponse<CategoryResponse>> fetchCategoriesInPages(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "CATEGORY_ID") CategorySortField sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.fetchCategoriesInPage(page, size, keyword, sortBy, sortDirection));
    }

    @GetMapping("/fetch/category-with-products")
    public ResponseEntity<PageResponse<CategoryWithProductResponse>> fetchCategoriesWithProductsInPages(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, defaultValue = "CATEGORY_ID") CategorySortField sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.fetchCategoriesWithProductsInPages(page, size, keyword, categoryId, sortBy, sortDirection));
    }
}
