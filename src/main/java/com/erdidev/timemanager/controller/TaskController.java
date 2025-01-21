package com.erdidev.timemanager.controller;

import com.erdidev.timemanager.dto.TaskDto;
import com.erdidev.timemanager.model.Priority;
import com.erdidev.timemanager.model.TaskStatus;
import com.erdidev.timemanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    @Operation(
        summary = "Get all tasks with pagination",
        description = "Returns a paginated list of tasks. Sort options: createdAt, title, status, priority, dueDate"
    )
    public ResponseEntity<Page<TaskDto>> getTasks(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                description = "Sort field and direction (e.g., createdAt,desc or title,asc)",
                example = "createdAt,desc"
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Direction direction = Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @GetMapping("/project/{projectId}")
    @Operation(
        summary = "Get tasks by project",
        description = "Returns a paginated list of tasks for a specific project. Sort options: createdAt, title, status, priority, dueDate"
    )
    public ResponseEntity<Page<TaskDto>> getTasksByProject(
            @PathVariable Long projectId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                description = "Sort field and direction (e.g., createdAt,desc or title,asc)",
                example = "createdAt,desc"
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Direction direction = Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "Get tasks by category",
        description = "Returns a paginated list of tasks for a specific category. Sort options: createdAt, title, status, priority, dueDate"
    )
    public ResponseEntity<Page<TaskDto>> getTasksByCategory(
            @PathVariable Long categoryId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                description = "Sort field and direction (e.g., createdAt,desc or title,asc)",
                example = "createdAt,desc"
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Direction direction = Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(taskService.getTasksByCategory(categoryId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status")
    public ResponseEntity<Page<TaskDto>> getTasksByStatus(
            @PathVariable TaskStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status, pageable));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority")
    public ResponseEntity<Page<TaskDto>> getTasksByPriority(
            @PathVariable Priority priority,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByPriority(priority, pageable));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks")
    public ResponseEntity<Page<TaskDto>> getOverdueTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getOverdueTasks(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks by title")
    public ResponseEntity<List<TaskDto>> searchTasks(
            @Parameter(description = "Search query") @RequestParam String query) {
        return ResponseEntity.ok(taskService.searchTasks(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(taskService.createTask(taskDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDto taskDto) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
} 