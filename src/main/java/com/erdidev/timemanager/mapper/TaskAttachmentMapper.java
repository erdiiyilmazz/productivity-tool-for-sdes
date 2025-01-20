package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.TaskAttachmentDto;
import com.erdidev.timemanager.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskAttachmentMapper {
    @Mapping(target = "task", ignore = true)
    TaskAttachment toEntity(TaskAttachmentDto dto);

    @Mapping(target = "taskId", source = "task.id")
    TaskAttachmentDto toDto(TaskAttachment attachment);

    @Mapping(target = "task", ignore = true)
    void updateEntity(TaskAttachmentDto dto, @MappingTarget TaskAttachment attachment);
} 