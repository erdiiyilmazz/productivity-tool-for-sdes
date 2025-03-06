package com.erdidev.timetracker.dto;

import com.erdidev.taskmanager.dto.BaseDto;
import com.erdidev.timetracker.model.TimeEntryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Time Entry Data Transfer Object")
public class TimeEntryDto extends BaseDto {
    
    @Schema(description = "Task ID", example = "1")
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @Schema(description = "Start time", example = "2023-01-01T09:00:00")
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @Schema(description = "End time", example = "2023-01-01T12:30:00")
    private LocalDateTime endTime;
    
    @Schema(description = "Duration in seconds", example = "12600")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long durationSeconds;
    
    @Schema(description = "Description of work done", example = "Implemented user authentication")
    private String description;
    
    @Schema(description = "User ID", example = "1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;
    
    @Schema(description = "Is this time entry billable", example = "true")
    private boolean billable;
    
    @Schema(description = "Status of the time entry", example = "RUNNING")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TimeEntryStatus status;
    
    @Schema(description = "Task title (read-only)", example = "Implement Authentication")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String taskTitle;
} 