package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.TaskDto;
import com.erdidev.timemanager.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "projectId", source = "project.id")
    TaskDto toDto(Task task);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Task toEntity(TaskDto taskDto);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateEntity(TaskDto taskDto, @MappingTarget Task task);
} 