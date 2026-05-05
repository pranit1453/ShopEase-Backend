package com.shopease.inventory.mapper;

import com.shopease.inventory.dto.CreateInventoryRequest;
import com.shopease.inventory.dto.CreateInventoryResponse;
import com.shopease.inventory.dto.UpdateInventoryRequest;
import com.shopease.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "productId", source = "productId"),
            @Mapping(target = "totalQuantity", source = "totalQuantity"),
            @Mapping(target = "reservedQuantity", source = "reservedQuantity")
    })
    Inventory mappedToInventory(final CreateInventoryRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "inventoryId", source = "inventoryId"),
            @Mapping(target = "productId", source = "productId"),
            @Mapping(target = "totalQuantity", source = "totalQuantity"),
            @Mapping(target = "reservedQuantity", source = "reservedQuantity")
    })
    CreateInventoryResponse mappedToCreateInventoryResponse(final Inventory inventory);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "inventoryId", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchInventory(final UpdateInventoryRequest request, @MappingTarget Inventory inventory);


}
