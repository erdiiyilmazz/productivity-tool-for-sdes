package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.TaskDto;
import com.erdidev.taskmanager.exception.*;
import com.erdidev.taskmanager.mapper.TaskMapper;
import com.erdidev.taskmanager.model.*;
import com.erdidev.taskmanager.model.Priority;
import com.erdidev.taskmanager.repository.CategoryRepository;
import com.erdidev.taskmanager.repository.ProjectRepository;
import com.erdidev.taskmanager.repository.TaskAttachmentRepository;
import com.erdidev.taskmanager.repository.TaskRepository;
import com.erdidev.common.util.SecurityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private TaskAttachmentRepository attachmentRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDto taskDto;
    private Project project;
    private Category category;
    private final Long testUserId = 1L;
    private static MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeAll
    static void setUpAll() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterAll
    static void tearDownAll() {
        // Clean up static mock after all tests
        securityUtilsMock.close();
    }

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setDescription("Test Description");
        category.setProject(project);
        
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setProject(project);
        task.setCategory(category);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setOwnerId(testUserId);
        
        taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setProjectId(1L);
        taskDto.setCategoryId(1L);
        taskDto.setStatus(TaskStatus.TODO);
        taskDto.setPriority(Priority.MEDIUM);
        taskDto.setDueDate(LocalDateTime.now().plusDays(1));
        
        // Configure SecurityUtils mock for each test
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
        
        // Setup default empty responses for repositories to avoid NPEs
        Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findByProjectId(anyLong(), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findByCategoryId(anyLong(), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findByStatus(any(TaskStatus.class), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findByPriority(any(Priority.class), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.findByTitleContainingIgnoreCase(anyString())).thenReturn(Collections.emptyList());
        doNothing().when(attachmentRepository).deleteByTaskId(anyLong());
    }

    @Test
    void getTasks_ReturnsPageOfTasks() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getTasks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        assertEquals(taskDto.getTitle(), result.getContent().getFirst().getTitle());
        verify(taskRepository).findAll(pageable);
    }

    @Test
    void getTasksByProject_ExistingProjectId_ReturnsTasksForProject() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findByProjectId(1L, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getTasksByProject(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        assertEquals(taskDto.getProjectId(), result.getContent().getFirst().getProjectId());
        verify(taskRepository).findByProjectId(1L, pageable);
    }

    @Test
    void getTasksByProject_NonExistingProjectId_ThrowsNotFoundException() {
        Pageable pageable = mock(Pageable.class);
        
        // Mock the implementation to throw the right exception
        when(taskRepository.findByProjectId(eq(999L), any(Pageable.class)))
            .thenThrow(new ProjectNotFoundException(999L));

        assertThrows(ProjectNotFoundException.class,
                () -> taskService.getTasksByProject(999L, pageable));
    }

    @Test
    void getTasksByCategory_ExistingCategoryId_ReturnsTasksForCategory() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findByCategoryId(1L, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getTasksByCategory(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        assertEquals(taskDto.getCategoryId(), result.getContent().getFirst().getCategoryId());
        verify(taskRepository).findByCategoryId(1L, pageable);
    }

    @Test
    void getTasksByCategory_NonExistingCategoryId_ThrowsNotFoundException() {
        Pageable pageable = mock(Pageable.class);
        
        // Mock the implementation to throw the right exception
        when(taskRepository.findByCategoryId(eq(999L), any(Pageable.class)))
            .thenThrow(new CategoryNotFoundException(999L));

        assertThrows(CategoryNotFoundException.class,
                () -> taskService.getTasksByCategory(999L, pageable));
    }

    @Test
    void getTasksByStatus_ReturnsTasksWithRequestedStatus() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findByStatus(TaskStatus.TODO, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getTasksByStatus(TaskStatus.TODO, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        assertEquals(TaskStatus.TODO, result.getContent().getFirst().getStatus());
        verify(taskRepository).findByStatus(TaskStatus.TODO, pageable);
    }

    @Test
    void getTasksByPriority_ReturnsTasksWithRequestedPriority() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findByPriority(Priority.MEDIUM, pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getTasksByPriority(Priority.MEDIUM, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        assertEquals(Priority.MEDIUM, result.getContent().getFirst().getPriority());
        verify(taskRepository).findByPriority(Priority.MEDIUM, pageable);
    }

    @Test
    void getOverdueTasks_ReturnsOverdueTasks() {
        Pageable pageable = mock(Pageable.class);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class), eq(pageable)))
            .thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        Page<TaskDto> result = taskService.getOverdueTasks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDto.getId(), result.getContent().getFirst().getId());
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void searchTasks_ReturnsMatchingTasks() {
        String searchQuery = "test";
        when(taskRepository.findByTitleContainingIgnoreCase(searchQuery))
            .thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        List<TaskDto> results = taskService.searchTasks(searchQuery);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(taskDto.getId(), results.getFirst().getId());
        assertEquals(taskDto.getTitle(), results.getFirst().getTitle());
        verify(taskRepository).findByTitleContainingIgnoreCase(searchQuery);
    }

    @Test
    void getTask_ExistingId_ReturnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.getTask(1L);

        assertNotNull(result);
        assertEquals(taskDto.getId(), result.getId());
        assertEquals(taskDto.getTitle(), result.getTitle());
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTask_NonExistingId_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(999L));
        verify(taskRepository).findById(999L);
    }

    @Test
    void createTask_ValidData_ReturnsCreatedTask() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(taskMapper.toEntity(taskDto)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.createTask(taskDto);

        assertNotNull(result);
        assertEquals(taskDto.getId(), result.getId());
        assertEquals(taskDto.getTitle(), result.getTitle());
        verify(projectRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_NonExistingProjectId_ThrowsException() {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle("Test Task");
        newTaskDto.setProjectId(999L);
        
        // Need to mock the mapper to return a non-null Task
        Task mappedTask = new Task();
        when(taskMapper.toEntity(newTaskDto)).thenReturn(mappedTask);
        
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> taskService.createTask(newTaskDto));
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_NonExistingCategoryId_ThrowsException() {
        TaskDto newTaskDto = new TaskDto();
        newTaskDto.setTitle("Test Task");
        newTaskDto.setProjectId(1L);
        newTaskDto.setCategoryId(999L);
        
        // Need to mock the mapper to return a non-null Task
        Task mappedTask = new Task();
        when(taskMapper.toEntity(newTaskDto)).thenReturn(mappedTask);
        
        // The service method checks category first, so project is never checked
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> taskService.createTask(newTaskDto));
        verify(categoryRepository).findById(999L);
        // Project repository is never called because exception is thrown earlier
        verify(projectRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ExistingId_ReturnsUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Task");
        updateDto.setDescription("Updated Description");
        updateDto.setProjectId(1L);
        updateDto.setCategoryId(1L);
        updateDto.setStatus(TaskStatus.IN_PROGRESS);
        updateDto.setPriority(Priority.HIGH);

        TaskDto result = taskService.updateTask(1L, updateDto);

        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository).findById(1L);
        verify(projectRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_NonExistingId_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Task");

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(999L, updateDto));
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_StatusChange_ReturnsUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto updateDto = new TaskDto();
        updateDto.setStatus(TaskStatus.DONE);

        TaskDto result = taskService.updateTask(1L, updateDto);

        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_StatusChange_NonExistingId_ThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskDto updateDto = new TaskDto();
        updateDto.setStatus(TaskStatus.DONE);

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(999L, updateDto));
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ExistingId_DeletesTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        assertDoesNotThrow(() -> taskService.deleteTask(1L));

        verify(taskRepository).existsById(1L);
        verify(attachmentRepository).deleteByTaskId(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_NonExistingId_ThrowsException() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(999L));
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(any());
        verify(attachmentRepository, never()).deleteByTaskId(any());
    }
} 