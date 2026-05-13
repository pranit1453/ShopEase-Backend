package com.shopease.permission.mapper;

import com.shopease.permission.dto.PermissionRequest;
import com.shopease.permission.dto.PermissionResponse;
import com.shopease.permission.entity.Permission;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "moduleName", source = "request.moduleName")
    @Mapping(target = "actionName", source = "request.actionName")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "permissionCode", source = "permissionCode")
    Permission toEntity(PermissionRequest request, String permissionCode);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "permissionId", source = "permissionId")
    @Mapping(target = "permissionCode", source = "permissionCode")
    PermissionResponse toResponse(Permission permission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "permissionId", ignore = true)
    Permission patchPermission(PermissionRequest request, @MappingTarget Permission permission);
}
