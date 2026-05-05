package com.shopease.category.service;

import com.shopease.category.dto.*;
import com.shopease.category.entity.Category;
import com.shopease.category.enums.CategorySortField;
import com.shopease.category.exception.custom.CategoryAlreadyExistsException;
import com.shopease.category.exception.custom.CategoryNotFoundException;
import com.shopease.category.exception.custom.ProductAssociatedWithCategoryException;
import com.shopease.category.mapper.CategoryMapper;
import com.shopease.category.repository.CategoryRepository;
import com.shopease.category.specification.CategorySpecification;
import com.shopease.common.wrapper.PageResponse;
import com.shopease.product.dto.ProductResponse;
import com.shopease.product.entity.Product;
import com.shopease.product.repository.ProductRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Set<CreateCategoryResponse> createCategoriesInBulk(Set<CreateCategoryRequest> request) {
        log.info("Creating categories in bulk. Count={}", request.size());

        Set<String> names = request.stream()
                .map(CreateCategoryRequest::name)
                .collect(Collectors.toSet());
        log.debug("Category names received: {}", names);

        Set<String> existingNames = categoryRepository.findExistingNames(names);

        if (!existingNames.isEmpty()) {
            log.warn("Categories already exist: {}", existingNames);
            throw new CategoryAlreadyExistsException("Categories already exist: " + existingNames);
        }
        List<Category> categories = request.stream()
                .map(categoryMapper::mappedToCategory)
                .collect(Collectors.toList());

        List<Category> savedCategories = categoryRepository.saveAll(categories);
        log.info("Successfully created {} categories", savedCategories.size());
        return savedCategories.stream()
                .map(categoryMapper::mappedToCategoryResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public CreateCategoryResponse createCategory(final CreateCategoryRequest request) {
        log.info("Creating category with name={}", request.name());

        if (categoryRepository.existsByName(request.name())) {
            log.warn("Category already exists with name={}", request.name());
            throw new CategoryAlreadyExistsException("Category with name " + request.name() + " already exist");
        }
        Category category = categoryMapper.mappedToCategory(request);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id={}", savedCategory.getCategoryId());
        return categoryMapper.mappedToCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public UpdateCategoryResponse updateCategoryById(final UUID id, final UpdateCategoryRequest request) {
        log.info("Updating category with id={}", id);
        Category category = findByCategoryId(id);
        log.debug("Existing category before update: {}", category);

        category.setName(request.name());
        category.setDescription(request.description());
        Category savedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id={}", id);
        return categoryMapper.mappedToUpdateCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public UpdateCategoryResponse patchCategoryById(final UUID id, final UpdateCategoryRequest request) {
        log.info("Patching category with id={}", id);
        Category category = findByCategoryId(id);
        categoryMapper.patchCategory(request, category);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category patched successfully with id={}", id);
        return categoryMapper.mappedToUpdateCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryById(final UUID id) {
        log.info("Deleting category with id={}", id);

        Category category = findByCategoryId(id);
        if (productRepository.existsByCategory_CategoryId(id)) {
            log.warn("Cannot delete category. Products are associated. categoryId={}", id);

            throw new ProductAssociatedWithCategoryException("Products are associated with this category");
        }
        categoryRepository.delete(category);
        log.info("Category deleted successfully with id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse fetchCategoryById(final UUID id) {
        log.debug("Fetching category by id={}", id);

        Category category = findByCategoryId(id);
        log.debug("Category fetched: {}", category);
        return categoryMapper.mappedToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> fetchCategoriesInPage(
            final int page,
            final int size,
            final String keyword,
            final CategorySortField sortBy,
            final String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(Sort.Direction.ASC, sortBy.getField())
                : Sort.by(Sort.Direction.DESC, sortBy.getField());
        log.info("Fetching categories page={}, size={}, keyword={}, sortBy={}, direction={}",
                page, size, keyword, sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Category> specification = CategorySpecification.searchKeyword(keyword);

        Page<Category> pages = categoryRepository.findAll(specification, pageable);
        log.debug("Fetched {} categories in fetchCategoriesInPage", pages.getNumberOfElements());
        List<CategoryResponse> content = pages.getContent()
                .stream()
                .map(categoryMapper::mappedToResponse)
                .toList();

        return PageResponse.<CategoryResponse>builder()
                .content(content)
                .page(pages.getNumber())
                .size(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    @Transactional
    public PageResponse<CategoryWithProductResponse> fetchCategoriesWithProductsInPages(
            final int page,
            final int size,
            final String keyword,
            final UUID categoryId,
            final CategorySortField sortBy,
            final String sortDirection
    ) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(Sort.Direction.ASC, sortBy.getField())
                : Sort.by(Sort.Direction.DESC, sortBy.getField());
        log.info("Fetching categories with products. page={}, size={}, keyword={}, categoryId={}",
                page, size, keyword, categoryId);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Category> specification =
                CategorySpecification.build(keyword, categoryId);
        // Fetching paginated categories
        Page<Category> categoryPage = categoryRepository.findAll(specification, pageable);
        log.debug("Fetched {} categories in fetchCategoriesWithProductsInPages", categoryPage.getNumberOfElements());

        // fetch products in bulk
        List<UUID> categoryIds = categoryPage.getContent()
                .stream()
                .map(Category::getCategoryId)
                .toList();
        log.debug("Fetching products for categoryIds={}", categoryIds);

        List<Product> products = categoryIds.isEmpty()
                ? List.of()
                : productRepository.findByCategoryIdIn(categoryIds);
        log.debug("Fetched {} products", products.size());
        // Grouping products
        Map<UUID, List<ProductResponse>> productMap = products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getCategoryId(),
                        Collectors.mapping(p -> ProductResponse.builder()
                                        .productId(p.getProductId())
                                        .name(p.getName())
                                        .description(p.getDescription())
                                        .price(p.getPrice())
                                        .build(),
                                Collectors.toList()
                        )));

        // mapping the response
        List<CategoryWithProductResponse> responses = categoryPage.getContent()
                .stream()
                .map(category -> CategoryWithProductResponse.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .products(productMap.getOrDefault(category.getCategoryId(), List.of()))
                        .build())
                .toList();

        return PageResponse.<CategoryWithProductResponse>builder()
                .content(responses)
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();
    }


    private Category findByCategoryId(final UUID id) {
        log.debug("Finding category by id={}", id);

        return categoryRepository.findByCategoryId(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id={}", id);
                    return new CategoryNotFoundException("Category with id " + id + " not found");
                });


    }


}
