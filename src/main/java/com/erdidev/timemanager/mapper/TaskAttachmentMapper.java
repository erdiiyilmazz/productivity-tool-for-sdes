package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.TaskAttachmentDto;
import com.erdidev.timemanager.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.nio.charset.StandardCharsets;

@Mapper(componentModel = "spring")
public interface TaskAttachmentMapper {
    @Mapping(target = "task", ignore = true)
    TaskAttachment toEntity(TaskAttachmentDto dto);

    @Mapping(source = "task.id", target = "taskId")
    TaskAttachmentDto toDto(TaskAttachment attachment);

    @Mapping(target = "task", ignore = true)
    void updateEntity(TaskAttachmentDto dto, @MappingTarget TaskAttachment attachment);

    default byte[] map(String value) {
        if (value == null) return null;
        return value.getBytes(StandardCharsets.UTF_8);
    }

    default String map(byte[] value) {
        if (value == null) return null;
        return new String(value, StandardCharsets.UTF_8);
    }
} 