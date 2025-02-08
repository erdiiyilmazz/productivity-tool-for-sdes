package com.erdidev.scheduler.dto;

import com.erdidev.scheduler.enums.NotificationChannel;
import com.erdidev.scheduler.enums.ReminderStatus;
import com.erdidev.timemanager.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReminderDto extends BaseDto {
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;
    
    @NotNull(message = "Message is required")
    private String message;
    
    @NotNull(message = "Reminder time is required")
    private LocalDateTime reminderTime;
    
    @NotNull(message = "Type is required")
    private NotificationChannel type;
    
    private ReminderStatus status;
    
    private Set<NotificationChannel> notificationChannels;
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 