package com.erdidev.timetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request to stop a running time entry")
public class StopTimeEntryRequest {
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of work done", example = "Implemented login form")
    private String description;
} 