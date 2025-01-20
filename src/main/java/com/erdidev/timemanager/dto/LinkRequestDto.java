package com.erdidev.timemanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Schema(description = "Link Upload Request")
public class LinkRequestDto {
    @Schema(description = "Name of the link", example = "API Documentation")
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "URL", example = "https://api.example.com/docs")
    @NotBlank(message = "URL is required")
    @URL(message = "Must be a valid URL")
    private String url;
} 