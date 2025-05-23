package com.erdidev.taskmanager.dto;

import com.erdidev.taskmanager.model.AttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Task Attachment Data Transfer Object")
public class TaskAttachmentDto extends BaseDto {
    
    @Schema(description = "Attachment name", example = "Project Requirements")
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "Attachment content", example = "File content or code snippet or URL")
    @NotBlank(message = "Content is required")
    private String content;
    
    @Schema(description = "File extension (for FILE and CODE_SNIPPET types)", example = "pdf")
    private String extension;
    
    @Schema(description = "Attachment type", example = "FILE")
    @NotNull(message = "Type is required")
    private AttachmentType type;
    
    @Schema(description = "Task ID", example = "1")
    private Long taskId;
    
    @Schema(description = "Owner ID", example = "1")
    private Long ownerId;

    // Custom validation based on type
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && type != null;
    }
} 