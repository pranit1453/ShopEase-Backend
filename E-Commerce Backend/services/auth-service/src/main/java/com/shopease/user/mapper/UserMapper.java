package com.shopease.user.mapper;

import com.shopease.user.dto.UpdateUserDetailsRequest;
import com.shopease.user.dto.UpdateUserDetailsResponse;
import com.shopease.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "username", source = "username"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "gender", source = "gender")
    })
    UpdateUserDetailsResponse toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "oauthId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "image",ignore = true)
    void patchUserDetails(UpdateUserDetailsRequest request, @MappingTarget User user);
}
