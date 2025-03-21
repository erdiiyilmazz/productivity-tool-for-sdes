package com.erdidev.taskmanager.mapper;

import com.erdidev.taskmanager.dto.TaskAttachmentDto;
import com.erdidev.taskmanager.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import java.nio.charset.StandardCharsets;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskAttachmentMapper {
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "content", ignore = true)
    TaskAttachment toEntity(TaskAttachmentDto dto);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(target = "content", expression = "java(mapToString(attachment.getContent()))")
    TaskAttachmentDto toDto(TaskAttachment attachment);

    @Mapping(target = "task", ignore = true)
    @Mapping(target = "content", ignore = true)
    void updateEntity(TaskAttachmentDto dto, @MappingTarget TaskAttachment attachment);

    default byte[] mapToBytes(String value) {
        if (value == null) return null;
        return value.getBytes(StandardCharsets.UTF_8);
    }

    default String mapToString(byte[] value) {
        if (value == null) return null;
        return new String(value, StandardCharsets.UTF_8);
    }
} 