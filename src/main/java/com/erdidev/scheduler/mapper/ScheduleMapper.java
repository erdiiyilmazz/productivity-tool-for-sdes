package com.erdidev.scheduler.mapper;

import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.model.Schedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    Schedule toEntity(ScheduleDto scheduleDto);
    ScheduleDto toDto(Schedule schedule);
} 