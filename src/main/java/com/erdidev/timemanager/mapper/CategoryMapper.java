package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.CategoryDto;
import com.erdidev.timemanager.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "project", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category category);

    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(CategoryDto categoryDto, @MappingTarget Category category);
} 