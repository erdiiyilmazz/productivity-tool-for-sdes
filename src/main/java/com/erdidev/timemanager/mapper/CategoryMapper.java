package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.CategoryDto;
import com.erdidev.timemanager.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    @Mapping(source = "project.id", target = "projectId")
    CategoryDto toDto(Category category);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntity(CategoryDto categoryDto, @MappingTarget Category category);
} 