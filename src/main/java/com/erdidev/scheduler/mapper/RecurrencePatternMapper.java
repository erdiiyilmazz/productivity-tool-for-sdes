package com.erdidev.scheduler.mapper;

import com.erdidev.scheduler.model.RecurrencePattern;
import com.erdidev.scheduler.dto.RecurrencePatternDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecurrencePatternMapper {
    RecurrencePatternDto toDto(RecurrencePattern pattern);
    RecurrencePattern toEntity(RecurrencePatternDto patternDto);
} 