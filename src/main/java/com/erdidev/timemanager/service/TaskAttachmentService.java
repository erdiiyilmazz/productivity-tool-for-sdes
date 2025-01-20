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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAttachmentService {
    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskAttachmentMapper attachmentMapper;

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
} 