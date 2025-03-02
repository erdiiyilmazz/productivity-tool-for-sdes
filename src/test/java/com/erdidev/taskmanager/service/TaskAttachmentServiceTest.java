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
import com.erdidev.common.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskAttachmentServiceTest {

    @Mock
    private TaskAttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskAttachmentMapper attachmentMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TaskAttachmentService attachmentService;

    private Task task;
    private TaskAttachment attachment;
    private TaskAttachmentDto attachmentDto;
    private final Long testUserId = 1L;
    private final String tempUploadDir = System.getProperty("java.io.tmpdir") + "/test-uploads";

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        
        attachment = new TaskAttachment();
        attachment.setId(1L);
        attachment.setName("Test Attachment");
        attachment.setTask(task);
        attachment.setType(AttachmentType.FILE);
        attachment.setContent("Test content".getBytes(StandardCharsets.UTF_8));
        
        attachmentDto = new TaskAttachmentDto();
        attachmentDto.setId(1L);
        attachmentDto.setName("Test Attachment");
        attachmentDto.setTaskId(1L);
        attachmentDto.setType(AttachmentType.FILE);
        attachmentDto.setContent("Test content");
        
        // Set the uploadDir field using reflection
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempUploadDir);
        
        // Create the directory
        try {
            Files.createDirectories(Paths.get(tempUploadDir));
        } catch (IOException e) {
            fail("Could not create temp directory for tests: " + e.getMessage());
        }
    }

    @Test
    void getTaskAttachments_ReturnsAttachmentsForTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(attachmentRepository.findByTaskId(1L)).thenReturn(List.of(attachment));
        when(attachmentMapper.toDto(attachment)).thenReturn(attachmentDto);

        List<TaskAttachmentDto> results = attachmentService.getTaskAttachments(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(attachmentDto.getId(), results.getFirst().getId());
        assertEquals(attachmentDto.getTaskId(), results.getFirst().getTaskId());
        verify(attachmentRepository).findByTaskId(1L);
    }

    @Test
    void getTaskAttachmentsByType_ReturnsAttachmentsWithRequestedType() {
        when(attachmentRepository.findByTaskIdAndType(1L, AttachmentType.FILE))
            .thenReturn(List.of(attachment));
        when(attachmentMapper.toDto(attachment)).thenReturn(attachmentDto);

        List<TaskAttachmentDto> results = attachmentService.getTaskAttachmentsByType(1L, AttachmentType.FILE);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(attachmentDto.getId(), results.getFirst().getId());
        assertEquals(AttachmentType.FILE, results.getFirst().getType());
        verify(attachmentRepository).findByTaskIdAndType(1L, AttachmentType.FILE);
    }

    @Test
    void addAttachment_ValidData_ReturnsCreatedAttachment() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(attachmentMapper.toEntity(attachmentDto)).thenReturn(attachment);
        when(attachmentRepository.save(any(TaskAttachment.class))).thenReturn(attachment);
        when(attachmentMapper.toDto(attachment)).thenReturn(attachmentDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            TaskAttachmentDto result = attachmentService.addAttachment(1L, attachmentDto);

            assertNotNull(result);
            assertEquals(attachmentDto.getId(), result.getId());
            assertEquals(attachmentDto.getName(), result.getName());
            verify(taskRepository).findById(1L);
            verify(attachmentRepository).save(any(TaskAttachment.class));
        }
    }

    @Test
    void addAttachment_NonExistingTaskId_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> attachmentService.addAttachment(999L, attachmentDto));
        verify(taskRepository).findById(999L);
        verify(attachmentRepository, never()).save(any(TaskAttachment.class));
    }

    @Test
    void createAttachment_ValidData_ReturnsCreatedAttachment() throws IOException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(multipartFile.getBytes()).thenReturn("Test content".getBytes(StandardCharsets.UTF_8));
        when(multipartFile.getOriginalFilename()).thenReturn("test_file.txt");
        // Mock the input stream
        when(multipartFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("Test content".getBytes(StandardCharsets.UTF_8)));
        when(attachmentMapper.toEntity(attachmentDto)).thenReturn(attachment);
        when(attachmentRepository.save(any(TaskAttachment.class))).thenReturn(attachment);
        when(attachmentMapper.toDto(attachment)).thenReturn(attachmentDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            TaskAttachmentDto result = attachmentService.createAttachment(attachmentDto, multipartFile);

            assertNotNull(result);
            assertEquals(attachmentDto.getId(), result.getId());
            assertEquals(attachmentDto.getName(), result.getName());
            verify(attachmentRepository).save(any(TaskAttachment.class));
        }
    }

    @Test
    void deleteAttachment_ExistingId_DeletesAttachment() {
        when(attachmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(attachmentRepository).deleteById(1L);

        assertDoesNotThrow(() -> attachmentService.deleteAttachment(1L));

        verify(attachmentRepository).existsById(1L);
        verify(attachmentRepository).deleteById(1L);
    }

    @Test
    void deleteAttachment_NonExistingId_ThrowsException() {
        when(attachmentRepository.existsById(999L)).thenReturn(false);

        assertThrows(TaskAttachmentNotFoundException.class, () -> attachmentService.deleteAttachment(999L));
        verify(attachmentRepository).existsById(999L);
        verify(attachmentRepository, never()).deleteById(any());
    }

    @Test
    void deleteAllTaskAttachments_ExistingTaskId_DeletesAllAttachments() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(attachmentRepository).deleteByTaskId(1L);

        assertDoesNotThrow(() -> attachmentService.deleteAllTaskAttachments(1L));

        verify(taskRepository).existsById(1L);
        verify(attachmentRepository).deleteByTaskId(1L);
    }

    @Test
    void deleteAllTaskAttachments_NonExistingTaskId_ThrowsException() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> attachmentService.deleteAllTaskAttachments(999L));
        verify(taskRepository).existsById(999L);
        verify(attachmentRepository, never()).deleteByTaskId(any());
    }
} 