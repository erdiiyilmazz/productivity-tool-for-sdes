package com.erdidev.timemanager.controller;

import com.erdidev.timemanager.dto.TaskAttachmentDto;
import com.erdidev.timemanager.model.AttachmentType;
import com.erdidev.timemanager.service.TaskAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Task Attachments", description = "APIs for managing task attachments")
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
} 