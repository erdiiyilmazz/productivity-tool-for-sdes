package com.erdidev.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Project Data Transfer Object")
public class ProjectDto extends BaseDto {
    
    @Schema(description = "Project name", example = "E-commerce Platform")
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;

    @Schema(description = "Project description", example = "Online shopping platform development")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
} 