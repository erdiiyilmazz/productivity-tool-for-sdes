package com.erdidev.scheduler.mapper;

import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.model.Schedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RecurrencePatternMapper.class})
public interface ScheduleMapper {
    ScheduleDto toDto(Schedule schedule);
    Schedule toEntity(ScheduleDto scheduleDto);
} 