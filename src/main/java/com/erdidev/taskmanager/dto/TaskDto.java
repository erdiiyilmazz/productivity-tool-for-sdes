package com.erdidev.taskmanager.dto;

import com.erdidev.taskmanager.model.Priority;
import com.erdidev.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDto extends BaseDto {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime dueDate;
    private Long categoryId;
    private Long projectId;
} 