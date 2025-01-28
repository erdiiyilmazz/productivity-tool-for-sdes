package com.erdidev.scheduler.dto;

import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.timemanager.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleDto extends BaseDto {
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;
    private ScheduleStatus status;
    private String timeZone;
    private String title;
    private String description;
} 