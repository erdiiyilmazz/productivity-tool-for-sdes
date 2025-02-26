package com.erdidev.scheduler.dto;

import com.erdidev.scheduler.enums.NotificationChannel;
import com.erdidev.scheduler.enums.ReminderStatus;
import com.erdidev.taskmanager.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Reminder Data Transfer Object")
public class ReminderDto extends BaseDto {
    
    @Schema(example = "1", description = "Schedule ID that this reminder belongs to")
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;
    
    @Schema(
        example = "Don't forget to review the pull request!",
        description = "Reminder message content"
    )
    @NotNull(message = "Message is required")
    private String message;
    
    @Schema(
        example = "2025-02-08T21:30:00+03:00",
        description = "When the reminder should be triggered (with timezone)"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @NotNull(message = "Reminder time is required")
    private ZonedDateTime reminderTime;
    
    @Schema(
        example = "WEBSOCKET",
        description = "Primary notification channel type"
    )
    @NotNull(message = "Type is required")
    private NotificationChannel type;
    
    @Schema(
        example = "PENDING",
        description = "Current status of the reminder"
    )
    private ReminderStatus status;
    
    @Schema(
        example = "[\"WEBSOCKET\"]",
        description = "List of notification channels to use"
    )
    private Set<NotificationChannel> notificationChannels;
    
    @Schema(example = "1", description = "Task ID that this reminder belongs to")
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 