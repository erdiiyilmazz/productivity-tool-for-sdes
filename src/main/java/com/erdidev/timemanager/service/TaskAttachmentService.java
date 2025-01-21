package com.erdidev.timemanager.service;

import com.erdidev.timemanager.dto.TaskAttachmentDto;
import com.erdidev.timemanager.exception.TaskAttachmentNotFoundException;
import com.erdidev.timemanager.exception.TaskNotFoundException;
import com.erdidev.timemanager.mapper.TaskAttachmentMapper;
import com.erdidev.timemanager.model.Task;
import com.erdidev.timemanager.model.TaskAttachment;
import com.erdidev.timemanager.model.AttachmentType;
import com.erdidev.timemanager.repository.TaskAttachmentRepository;
import com.erdidev.timemanager.repository.TaskRepository;
import com.erdidev.timemanager.util.AttachmentValidationUtil;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAttachmentService {
    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskAttachmentMapper attachmentMapper;

    @Value("${timemanager.attachments.upload-dir}")
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
    public TaskAttachmentDto createAttachment(TaskAttachmentDto dto, MultipartFile file) {
        validateAttachment(dto, file);

        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(dto.getTaskId()));
        
        TaskAttachment attachment = attachmentMapper.toEntity(dto);
        attachment.setTask(task);
        
        switch (dto.getType()) {
            case FILE:
                handleFileUpload(attachment, file);
                break;
                
            case CODE_SNIPPET:
                AttachmentValidationUtil.validateCodeSnippet(dto.getContent());
                attachment.setContent(dto.getContent());
                break;
                
            case LINK:
                AttachmentValidationUtil.validateLink(dto.getContent());
                attachment.setContent(dto.getContent());
                break;
        }
        
        TaskAttachment savedAttachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(savedAttachment);
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
            attachment.setContent(storedFileName);
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file", ex);
        }
    }
} 