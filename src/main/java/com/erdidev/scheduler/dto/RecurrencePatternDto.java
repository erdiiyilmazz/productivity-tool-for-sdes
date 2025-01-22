package com.erdidev.scheduler.dto;

import com.erdidev.scheduler.enums.RecurrenceType;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RecurrencePatternDto {
    private RecurrenceType type;
    private Integer interval;
    private Set<DayOfWeek> daysOfWeek;
    private Integer dayOfMonth;
    private LocalDateTime endDate;
    private Integer occurrences;
} 