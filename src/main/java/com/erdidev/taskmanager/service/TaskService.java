package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.TaskDto;
import com.erdidev.taskmanager.exception.CategoryNotFoundException;
import com.erdidev.taskmanager.exception.ProjectNotFoundException;
import com.erdidev.taskmanager.exception.TaskNotFoundException;
import com.erdidev.taskmanager.mapper.TaskMapper;
import com.erdidev.taskmanager.model.*;
import com.erdidev.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable pageable) {
        log.debug("Fetching tasks page: {}", pageable);
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByProject(Long projectId, Pageable pageable) {
        log.debug("Fetching tasks for project: {}", projectId);
        return taskRepository.findByProjectId(projectId, pageable)
                .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching tasks for category: {}", categoryId);
        return taskRepository.findByCategoryId(categoryId, pageable)
                .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status, pageable)
                .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByPriority(Priority priority, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByPriority(priority, pageable);
        return tasks.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getOverdueTasks(Pageable pageable) {
        log.debug("Fetching overdue tasks");
        return taskRepository.findOverdueTasks(LocalDateTime.now(), pageable)
                .map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(Long id) {
        log.debug("Fetching task with id: {}", id);
        return taskRepository.findById(id)
                .map(taskMapper::toDto)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        log.debug("Creating task: {}", taskDto);
        Task task = taskMapper.toEntity(taskDto);
        
        // Set default status and priority if not provided
        task.setStatus(taskDto.getStatus() != null ? taskDto.getStatus() : TaskStatus.TODO);
        task.setPriority(taskDto.getPriority() != null ? taskDto.getPriority() : Priority.MEDIUM);
        
        // Assign to category if provided
        if (taskDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(taskDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(taskDto.getCategoryId()));
            task.setCategory(category);
        }
        
        // Assign to project if provided
        if (taskDto.getProjectId() != null) {
            Project project = projectRepository.findById(taskDto.getProjectId())
                    .orElseThrow(() -> new ProjectNotFoundException(taskDto.getProjectId()));
            task.setProject(project);
        }
        
        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        log.debug("Updating task with id: {}", id);
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        taskMapper.updateEntity(taskDto, existingTask);
        
        // Update category if provided
        if (taskDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(taskDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(taskDto.getCategoryId()));
            existingTask.setCategory(category);
        }
        
        // Update project if provided
        if (taskDto.getProjectId() != null) {
            Project project = projectRepository.findById(taskDto.getProjectId())
                    .orElseThrow(() -> new ProjectNotFoundException(taskDto.getProjectId()));
            existingTask.setProject(project);
        }
        
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.debug("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        // Delete associated attachments first
        attachmentRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> searchTasks(String query) {
        log.debug("Searching tasks with query: {}", query);
        return taskRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> searchTasks(String query, Pageable pageable) {
        return taskRepository.searchTasks(query, pageable)
                .map(taskMapper::toDto);
    }
} 