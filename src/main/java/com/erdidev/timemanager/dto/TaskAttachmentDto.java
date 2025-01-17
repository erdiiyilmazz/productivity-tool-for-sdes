package com.erdidev.timemanager.dto;

import com.erdidev.timemanager.model.AttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskAttachmentDto extends BaseDto {
    @NotBlank(message = "File name is required")
    private String fileName;
    
    private String fileType;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @NotNull(message = "Attachment type is required")
    private AttachmentType type;
    
    private Long taskId;
} 