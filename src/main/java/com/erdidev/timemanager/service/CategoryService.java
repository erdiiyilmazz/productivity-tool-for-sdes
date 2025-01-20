package com.erdidev.timemanager.service;

import com.erdidev.timemanager.dto.CategoryDto;
import com.erdidev.timemanager.exception.CategoryNotFoundException;
import com.erdidev.timemanager.exception.ProjectNotFoundException;
import com.erdidev.timemanager.mapper.CategoryMapper;
import com.erdidev.timemanager.model.Category;
import com.erdidev.timemanager.model.Project;
import com.erdidev.timemanager.repository.CategoryRepository;
import com.erdidev.timemanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategoriesByProject(Long projectId, Pageable pageable) {
        log.debug("Fetching categories for project id: {} with pagination: {}", projectId, pageable);
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
        return categoryRepository.findByProjectId(projectId, pageable)
                .map(categoryMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String query) {
        log.debug("Searching categories with query: {}", query);
        return categoryRepository.findByNameContainingIgnoreCase(query).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long id) {
        log.debug("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryDto createCategory(Long projectId, CategoryDto categoryDto) {
        log.debug("Creating category for project id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        
        Category category = categoryMapper.toEntity(categoryDto);
        category.setProject(project);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        log.debug("Updating category with id: {}", id);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        
        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }
} 