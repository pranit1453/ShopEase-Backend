package com.shopease.role.mapper;

import com.shopease.role.dto.RoleRequest;
import com.shopease.role.dto.RoleResponse;
import com.shopease.role.entity.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "roleName", source = "roleName")
    @Mapping(target = "description", source = "description")
    Role toEntity(RoleRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "roleId", source = "roleId")
    @Mapping(target = "roleName", source = "roleName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "permissions", source = "permissions")
    RoleResponse toResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roleId", ignore = true)
    Role patchRole(RoleRequest request, @MappingTarget Role role);
}
