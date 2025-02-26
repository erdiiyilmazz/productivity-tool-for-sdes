package com.erdidev.authmanager.mapper;

import com.erdidev.authmanager.dto.UserDto;
import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserDto toDto(User user);
    
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto userDto);
    
    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
} 