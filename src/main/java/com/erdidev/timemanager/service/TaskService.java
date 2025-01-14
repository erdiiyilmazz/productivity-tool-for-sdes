package com.erdidev.timemanager.service;

import com.erdidev.timemanager.dto.TaskDto;
import com.erdidev.timemanager.exception.TaskNotFoundException;
import com.erdidev.timemanager.mapper.TaskMapper;
import com.erdidev.timemanager.model.Task;
import com.erdidev.timemanager.model.TaskStatus;
import com.erdidev.timemanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable pageable) {
        log.debug("Fetching tasks page: {}", pageable);
        return taskRepository.findAll(pageable)
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
        taskDto.setStatus(TaskStatus.TODO); // Default status for new tasks
        Task task = taskMapper.toEntity(taskDto);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        log.debug("Updating task with id: {}", id);
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        taskMapper.updateEntity(taskDto, existingTask);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.debug("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
} 