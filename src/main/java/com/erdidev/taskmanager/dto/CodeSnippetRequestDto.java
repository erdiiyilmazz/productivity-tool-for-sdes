package com.erdidev.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Code Snippet Upload Request")
public class CodeSnippetRequestDto {
    @Schema(description = "Name of the code snippet", example = "Authentication Logic")
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "The code content", example = "public class Auth {\n    // code here\n}")
    @NotBlank(message = "Code content is required")
    private String code;
    
    @Schema(description = "Programming language", example = "java")
    private String language;
} 