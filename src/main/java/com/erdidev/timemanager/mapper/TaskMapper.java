package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.TaskDto;
import com.erdidev.timemanager.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface TaskMapper {
    TaskDto toDto(Task task);
    Task toEntity(TaskDto taskDto);
    void updateEntity(TaskDto taskDto, @MappingTarget Task task);
} 