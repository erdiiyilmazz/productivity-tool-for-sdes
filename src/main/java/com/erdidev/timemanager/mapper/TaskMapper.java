package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.TaskDto;
import com.erdidev.timemanager.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface TaskMapper {
    TaskDto toDto(Task task);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskDto taskDto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TaskDto taskDto, @MappingTarget Task task);
} 