package com.erdidev.taskmanager.controller;

import com.erdidev.taskmanager.dto.CodeSnippetRequestDto;
import com.erdidev.taskmanager.dto.LinkRequestDto;
import com.erdidev.taskmanager.dto.TaskAttachmentDto;
import com.erdidev.taskmanager.model.AttachmentType;
import com.erdidev.taskmanager.service.TaskAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Task Attachment Management", description = "APIs for managing task attachments")
public class TaskAttachmentController {
    private final TaskAttachmentService attachmentService;

    @GetMapping
    @Operation(summary = "Get all attachments for a task")
    public ResponseEntity<List<TaskAttachmentDto>> getTaskAttachments(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(attachmentService.getTaskAttachments(taskId));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get task attachments by type")
    public ResponseEntity<List<TaskAttachmentDto>> getTaskAttachmentsByType(
            @PathVariable Long taskId,
            @PathVariable AttachmentType type) {
        return ResponseEntity.ok(attachmentService.getTaskAttachmentsByType(taskId, type));
    }

    @PostMapping
    @Operation(summary = "Add an attachment to a task")
    public ResponseEntity<TaskAttachmentDto> addTaskAttachment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAttachmentDto attachmentDto) {
        return new ResponseEntity<>(attachmentService.addAttachment(taskId, attachmentDto), 
                HttpStatus.CREATED);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete a task attachment")
    public ResponseEntity<Void> deleteTaskAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all attachments for a task")
    public ResponseEntity<Void> deleteAllTaskAttachments(@PathVariable Long taskId) {
        attachmentService.deleteAllTaskAttachments(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload file attachment",
        description = "Upload a file attachment. Name and extension will be extracted from file."
    )
    public ResponseEntity<TaskAttachmentDto> uploadFileAttachment(
            @PathVariable Long taskId,
            @Parameter(description = "File to upload")
            @RequestPart MultipartFile file,
            @Parameter(description = "Custom name (optional)")
            @RequestParam(required = false) String name) {
        
        TaskAttachmentDto attachmentDto = new TaskAttachmentDto();
        attachmentDto.setType(AttachmentType.FILE);
        attachmentDto.setTaskId(taskId);
        attachmentDto.setName(name);
        
        return ResponseEntity.ok(attachmentService.createAttachment(attachmentDto, file));
    }

    @PostMapping("/upload/code")
    @Operation(
        summary = "Upload code snippet",
        description = "Add a code snippet with syntax highlighting support"
    )
    public ResponseEntity<TaskAttachmentDto> uploadCodeSnippet(
            @PathVariable Long taskId,
            @RequestBody CodeSnippetRequestDto request) {
        
        TaskAttachmentDto attachmentDto = new TaskAttachmentDto();
        attachmentDto.setType(AttachmentType.CODE_SNIPPET);
        attachmentDto.setName(request.getName());
        attachmentDto.setContent(request.getCode());
        attachmentDto.setExtension(request.getLanguage());
        attachmentDto.setTaskId(taskId);
        
        return ResponseEntity.ok(attachmentService.createAttachment(attachmentDto, null));
    }

    @PostMapping("/upload/link")
    @Operation(
        summary = "Add link attachment",
        description = "Add a URL link as attachment"
    )
    public ResponseEntity<TaskAttachmentDto> uploadLink(
            @PathVariable Long taskId,
            @Valid @RequestBody LinkRequestDto request) {
        
        return ResponseEntity.ok(
            attachmentService.createLinkAttachment(taskId, request.getName(), request.getUrl())
        );
    }
} 