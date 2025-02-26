package com.erdidev.taskmanager.mapper;

import com.erdidev.taskmanager.dto.TaskAttachmentDto;
import com.erdidev.taskmanager.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.nio.charset.StandardCharsets;

@Mapper(componentModel = "spring")
public interface TaskAttachmentMapper {
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "task", ignore = true)
    TaskAttachment toEntity(TaskAttachmentDto dto);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(target = "content", expression = "java(attachment.getContent() != null ? new String(attachment.getContent(), java.nio.charset.StandardCharsets.UTF_8) : null)")
    TaskAttachmentDto toDto(TaskAttachment attachment);

    @Mapping(target = "content", ignore = true)
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