package com.shopease.product.mapper;

import com.shopease.product.dto.*;
import com.shopease.product.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    Product mappedToProduct(final CreateProductRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "categoryId", source = "category.categoryId")
    CreateProductResponse mappedToCreateProductResponse(final Product product);


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    UpdateProductResponse mappedToUpdateProductResponse(final Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    void patchProduct(final UpdateProductRequest request, @MappingTarget final Product product);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    ProductResponse mappedToProductResponse(final Product product);
}
