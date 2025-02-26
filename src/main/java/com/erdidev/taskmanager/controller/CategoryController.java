package com.erdidev.taskmanager.controller;

import com.erdidev.taskmanager.dto.CategoryDto;
import com.erdidev.taskmanager.service.CategoryService;
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
@RequestMapping("/api/v1/projects/{projectId}/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(
        summary = "Get categories by project",
        description = "Returns a paginated list of categories for a project. Sort options: createdAt, name"
    )
    public ResponseEntity<Page<CategoryDto>> getCategories(
            @PathVariable Long projectId,
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
        
        return ResponseEntity.ok(categoryService.getCategoriesByProject(projectId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryDto> getCategory(
            @PathVariable Long projectId,
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @PostMapping
    @Operation(summary = "Create a new category in project")
    public ResponseEntity<CategoryDto> createCategory(
            @PathVariable Long projectId,
            @Valid @RequestBody CategoryDto categoryDto) {
        return new ResponseEntity<>(categoryService.createCategory(projectId, categoryDto), 
            HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long projectId,
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories by name")
    public ResponseEntity<List<CategoryDto>> searchCategories(
            @PathVariable Long projectId,
            @Parameter(description = "Search query") @RequestParam String query) {
        return ResponseEntity.ok(categoryService.searchCategories(query));
    }
} 