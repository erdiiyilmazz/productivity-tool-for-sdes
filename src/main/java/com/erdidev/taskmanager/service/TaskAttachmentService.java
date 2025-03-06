package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.TaskAttachmentDto;
import com.erdidev.taskmanager.exception.TaskAttachmentNotFoundException;
import com.erdidev.taskmanager.exception.TaskNotFoundException;
import com.erdidev.taskmanager.mapper.TaskAttachmentMapper;
import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.model.TaskAttachment;
import com.erdidev.taskmanager.model.AttachmentType;
import com.erdidev.taskmanager.repository.TaskAttachmentRepository;
import com.erdidev.taskmanager.repository.TaskRepository;
import com.erdidev.taskmanager.util.AttachmentValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

import com.erdidev.common.util.SecurityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAttachmentService {
    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskAttachmentMapper attachmentMapper;

    @Value("${taskmanager.attachments.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TaskAttachmentDto> getTaskAttachments(Long taskId) {
        log.debug("Fetching attachments for task: {}", taskId);
        return attachmentRepository.findByTaskId(taskId).stream()
                .map(attachmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskAttachmentDto> getTaskAttachmentsByType(Long taskId, AttachmentType type) {
        log.debug("Fetching attachments for task: {} with type: {}", taskId, type);
        return attachmentRepository.findByTaskIdAndType(taskId, type).stream()
                .map(attachmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskAttachmentDto addAttachment(Long taskId, TaskAttachmentDto attachmentDto) {
        log.debug("Adding attachment to task: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskAttachment attachment = attachmentMapper.toEntity(attachmentDto);
        attachment.setTask(task);
        attachment.setOwnerId(SecurityUtils.getCurrentUserId());
        
        // Convert content to bytes
        if (attachmentDto.getContent() != null) {
            attachment.setContent(attachmentDto.getContent().getBytes(StandardCharsets.UTF_8));
        }
        
        TaskAttachment savedAttachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(savedAttachment);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        log.debug("Deleting attachment: {}", attachmentId);
        if (!attachmentRepository.existsById(attachmentId)) {
            throw new TaskAttachmentNotFoundException(attachmentId);
        }
        attachmentRepository.deleteById(attachmentId);
    }

    @Transactional
    public void deleteAllTaskAttachments(Long taskId) {
        log.debug("Deleting all attachments for task: {}", taskId);
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        attachmentRepository.deleteByTaskId(taskId);
    }

    @Transactional
    public TaskAttachmentDto createAttachment(TaskAttachmentDto attachmentDto, MultipartFile file) {
        validateAttachment(attachmentDto, file);
        
        Task task = taskRepository.findById(attachmentDto.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(attachmentDto.getTaskId()));
        
        // Create attachment manually instead of using mapper
        TaskAttachment attachment = new TaskAttachment();
        attachment.setName(attachmentDto.getName());
        attachment.setType(attachmentDto.getType());
        attachment.setTask(task);
        attachment.setOwnerId(SecurityUtils.getCurrentUserId());
        
        if (attachmentDto.getExtension() != null) {
            attachment.setExtension(attachmentDto.getExtension());
        }
        
        // Handle content based on type
        if (attachmentDto.getType() == AttachmentType.FILE) {
            if (file != null) {
                handleFileUpload(attachment, file);
            }
        } else if (attachmentDto.getType() == AttachmentType.CODE_SNIPPET || 
                  attachmentDto.getType() == AttachmentType.LINK) {
            if (attachmentDto.getContent() != null) {
                // Explicitly convert string content to byte array
                attachment.setContent(attachmentDto.getContent().getBytes(StandardCharsets.UTF_8));
            }
        }
        
        // Save the attachment
        TaskAttachment savedAttachment = attachmentRepository.save(attachment);
        
        // Use mapper to convert to DTO
        TaskAttachmentDto dto = attachmentMapper.toDto(savedAttachment);
        
        // Ensure content is preserved
        if (attachmentDto.getContent() != null) {
            dto.setContent(attachmentDto.getContent());
        }
        
        return dto;
    }

    private void validateAttachment(TaskAttachmentDto dto, MultipartFile file) {
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid attachment data");
        }

        if (dto.getType() == AttachmentType.FILE && (file == null || file.isEmpty())) {
            throw new IllegalArgumentException("File is required for FILE type attachments");
        }
    }

    private void handleFileUpload(TaskAttachment attachment, MultipartFile file) {
        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            
            // Validate file name and path
            AttachmentValidationUtil.validateFileUpload(fileName);
            
            // Extract extension safely
            String extension = "";
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            }
            
            // Validate extension
            AttachmentValidationUtil.validateExtension(extension, attachment.getType());

            // Generate safe filename with UUID
            String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            Path targetLocation = Paths.get(uploadDir).resolve(storedFileName);
            
            // Validate path is within upload directory
            if (!targetLocation.normalize().startsWith(Paths.get(uploadDir).normalize())) {
                throw new SecurityException("Invalid file path");
            }
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            attachment.setContent(storedFileName.getBytes(StandardCharsets.UTF_8));
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file", ex);
        }
    }

    private TaskAttachment createAttachment(TaskAttachmentDto dto, Task task) {
        TaskAttachment attachment = attachmentMapper.toEntity(dto);
        attachment.setTask(task);
        
        // Convert String content to byte[]
        if (dto.getContent() != null) {
            attachment.setContent(dto.getContent().getBytes(StandardCharsets.UTF_8));
        }
        
        return attachmentRepository.save(attachment);
    }

    private void updateAttachment(TaskAttachment attachment, TaskAttachmentDto dto) {
        attachmentMapper.updateEntity(dto, attachment);
        
        // Convert String content to byte[]
        if (dto.getContent() != null) {
            attachment.setContent(dto.getContent().getBytes(StandardCharsets.UTF_8));
        }
        
        attachmentRepository.save(attachment);
    }

    @Transactional
    public TaskAttachmentDto createLinkAttachment(Long taskId, String name, String url) {
        log.debug("Creating link attachment for task: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        // Create attachment manually
        TaskAttachment attachment = new TaskAttachment();
        attachment.setName(name);
        attachment.setType(AttachmentType.LINK);
        attachment.setTask(task);
        attachment.setOwnerId(SecurityUtils.getCurrentUserId());
        
        // Explicitly convert URL to bytes
        if (url != null) {
            attachment.setContent(url.getBytes(StandardCharsets.UTF_8));
        }
        
        // Save the attachment
        TaskAttachment savedAttachment = attachmentRepository.save(attachment);
        
        // Use mapper to convert to DTO
        TaskAttachmentDto dto = attachmentMapper.toDto(savedAttachment);
        
        // Ensure content is set in the DTO
        dto.setContent(url);
        
        return dto;
    }
} 