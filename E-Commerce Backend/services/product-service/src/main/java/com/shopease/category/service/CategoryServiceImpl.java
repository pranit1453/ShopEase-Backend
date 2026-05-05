package com.shopease.category.service;

import com.shopease.category.dto.*;
import com.shopease.common.category.dto.*;
import com.shopease.category.entity.Category;
import com.shopease.category.enums.CategorySortField;
import com.shopease.category.mapper.CategoryMapper;
import com.shopease.category.repository.CategoryRepository;
import com.shopease.category.specification.CategorySpecification;
import com.shopease.exception.custom.DuplicateResourceFoundException;
import com.shopease.exception.custom.ResourceConflictException;
import com.shopease.product.dto.ProductResponse;
import com.shopease.product.entity.Product;
import com.shopease.product.repository.ProductRepository;
import com.shopease.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
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
        Set<String> names = request.stream()
                .map(CreateCategoryRequest::name)
                .collect(Collectors.toSet());

        Set<String> existingNames = categoryRepository.findExistingNames(names);

        if (!existingNames.isEmpty()) {
            throw new DuplicateResourceFoundException("Categories already exist: " + existingNames);
        }
        List<Category> categories = request.stream()
                .map(categoryMapper::mappedToCategory)
                .collect(Collectors.toList());

        List<Category> savedCategories = categoryRepository.saveAll(categories);
        return savedCategories.stream()
                .map(categoryMapper::mappedToCategoryResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public CreateCategoryResponse createCategory(final CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceFoundException("Category with name " + request.name() + " already exist");
        }
        Category category = categoryMapper.mappedToCategory(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.mappedToCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public UpdateCategoryResponse updateCategoryById(final UUID id, final UpdateCategoryRequest request) {
        Category category = findByCategoryId(id);
        category.setName(request.name());
        category.setDescription(request.description());
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.mappedToUpdateCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public UpdateCategoryResponse patchCategoryById(final UUID id, final UpdateCategoryRequest request) {
        Category category = findByCategoryId(id);
        categoryMapper.patchCategory(request, category);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.mappedToUpdateCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryById(final UUID id) {
        Category category = findByCategoryId(id);
        if (productRepository.existsByCategory_CategoryId(id)) {
            throw new ResourceConflictException("Products are associated with this category");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse fetchCategoryById(final UUID id) {
        Category category = findByCategoryId(id);
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

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Category> specification = CategorySpecification.searchKeyword(keyword);

        Page<Category> pages = categoryRepository.findAll(specification, pageable);
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
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Category> specification =
                CategorySpecification.build(keyword, categoryId);
        // Fetching paginated categories
        Page<Category> categoryPage = categoryRepository.findAll(specification, pageable);
        // fetch products in bulk
        List<UUID> categoryIds = categoryPage.getContent()
                .stream()
                .map(Category::getCategoryId)
                .toList();
        List<Product> products = categoryIds.isEmpty()
                ? List.of()
                : productRepository.findByCategoryIdIn(categoryIds);
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
        return categoryRepository.findByCategoryId(id)
                .orElseThrow(() ->
                        new CustomResourceNotFoundException("Category with id " + id + " not found"));
    }


}
