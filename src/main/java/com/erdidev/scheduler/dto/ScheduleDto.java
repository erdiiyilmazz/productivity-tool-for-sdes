package com.erdidev.scheduler.dto;

import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.timemanager.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Schedule Data Transfer Object")
public class ScheduleDto extends BaseDto {
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ScheduleStatus status;
    private String timeZone;
    private String title;
    private String description;

    @Schema(description = "Whether to create a default reminder", example = "true")
    private Boolean createDefaultReminder = false;  // Default to false
} 