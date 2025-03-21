package com.erdidev.taskmanager.mapper;

import com.erdidev.taskmanager.dto.TaskDto;
import com.erdidev.taskmanager.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, CategoryMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    Task toEntity(TaskDto taskDto);

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "category.id", target = "categoryId")
    TaskDto toDto(Task task);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    void updateEntity(TaskDto taskDto, @MappingTarget Task task);
} 