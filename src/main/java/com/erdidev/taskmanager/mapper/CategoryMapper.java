package com.erdidev.taskmanager.mapper;

import com.erdidev.taskmanager.dto.CategoryDto;
import com.erdidev.taskmanager.model.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    @Mapping(source = "project.id", target = "projectId")
    CategoryDto toDto(Category category);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CategoryDto categoryDto, @MappingTarget Category category);

    @AfterMapping
    default void linkTasks(@MappingTarget Category category) {
        if (category.getTasks() != null) {
            category.getTasks().forEach(task -> task.setCategory(category));
        }
    }
} 