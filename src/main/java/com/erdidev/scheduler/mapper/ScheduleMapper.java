package com.erdidev.scheduler.mapper;

import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.model.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleMapper {
    
    @Mapping(target = "task", ignore = true)
    Schedule toEntity(ScheduleDto scheduleDto);

    @Mapping(source = "task.id", target = "taskId")
    ScheduleDto toDto(Schedule schedule);

    @Mapping(target = "task", ignore = true)
    void updateEntity(ScheduleDto scheduleDto, @MappingTarget Schedule schedule);
} 