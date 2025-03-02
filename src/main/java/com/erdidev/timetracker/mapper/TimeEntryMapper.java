package com.erdidev.timetracker.mapper;

import com.erdidev.timetracker.dto.TimeEntryDto;
import com.erdidev.timetracker.model.TimeEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {
    
    @Mapping(target = "task", ignore = true)
    TimeEntry toEntity(TimeEntryDto dto);
    
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "task.title", target = "taskTitle")
    TimeEntryDto toDto(TimeEntry entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(TimeEntryDto dto, @MappingTarget TimeEntry entity);
} 