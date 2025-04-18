package com.erdidev.taskmanager.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.erdidev.taskmanager.dto.ProjectDto;
import com.erdidev.taskmanager.service.ProjectService;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.erdidev.taskmanager.exception.ProjectNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "APIs for managing projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    @Operation(
        summary = "Get all projects with pagination",
        description = "Returns a paginated list of projects. Sort options: createdAt, name"
    )
    public ResponseEntity<Page<ProjectDto>> getProjects(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                description = "Sort field and direction (e.g., createdAt,desc or name,asc)",
                example = "createdAt,desc"
            )
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Direction direction = Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(projectService.getProjects(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        return new ResponseEntity<>(projectService.createProject(projectDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing project",
        description = "Updates a project's name and description"
    )
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto projectDto) {
        try {
            return ResponseEntity.ok(projectService.updateProject(id, projectDto));
        } catch (ProjectNotFoundException ex) {
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search projects by name")
    public ResponseEntity<List<ProjectDto>> searchProjects(
            @Parameter(description = "Search query") @RequestParam String query) {
        return ResponseEntity.ok(projectService.searchProjects(query));
    }
} 