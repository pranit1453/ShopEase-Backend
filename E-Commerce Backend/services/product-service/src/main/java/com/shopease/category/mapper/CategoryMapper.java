package com.shopease.category.mapper;

import com.shopease.category.dto.*;
import com.shopease.category.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    Category mappedToCategory(final CreateCategoryRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    CreateCategoryResponse mappedToCategoryResponse(final Category category);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    UpdateCategoryResponse mappedToUpdateCategoryResponse(final Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "products", ignore = true)
    void patchCategory(final UpdateCategoryRequest request, @MappingTarget Category category);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    CategoryResponse mappedToResponse(final Category category);
}
