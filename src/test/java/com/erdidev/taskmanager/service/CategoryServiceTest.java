package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.CategoryDto;
import com.erdidev.taskmanager.exception.CategoryNotFoundException;
import com.erdidev.taskmanager.exception.ProjectNotFoundException;
import com.erdidev.taskmanager.mapper.CategoryMapper;
import com.erdidev.taskmanager.model.Category;
import com.erdidev.taskmanager.model.Project;
import com.erdidev.taskmanager.repository.CategoryRepository;
import com.erdidev.taskmanager.repository.ProjectRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private Project project;
    private final Long testUserId = 1L;

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
        category.setOwnerId(testUserId);
        
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Test Description");
        categoryDto.setProjectId(1L);
    }

    @Test
    void getCategories_ReturnsPageOfCategories() {
        Pageable pageable = mock(Pageable.class);
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        Page<CategoryDto> result = categoryService.getCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryDto.getId(), result.getContent().getFirst().getId());
        assertEquals(categoryDto.getName(), result.getContent().getFirst().getName());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getCategoriesByProject_ExistingProjectId_ReturnsCategoriesForProject() {
        Pageable pageable = mock(Pageable.class);
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByProjectId(1L, pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        Page<CategoryDto> result = categoryService.getCategoriesByProject(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryDto.getId(), result.getContent().getFirst().getId());
        assertEquals(categoryDto.getProjectId(), result.getContent().getFirst().getProjectId());
        verify(projectRepository).existsById(1L);
        verify(categoryRepository).findByProjectId(1L, pageable);
    }

    @Test
    void getCategoriesByProject_NonExistingProjectId_ThrowsException() {
        Pageable pageable = mock(Pageable.class);
        when(projectRepository.existsById(999L)).thenReturn(false);

        assertThrows(ProjectNotFoundException.class,
                () -> categoryService.getCategoriesByProject(999L, pageable));
        verify(projectRepository).existsById(999L);
        verify(categoryRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void searchCategories_ReturnsMatchingCategories() {
        String searchQuery = "test";
        when(categoryRepository.findByNameContainingIgnoreCase(searchQuery)).thenReturn(List.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        List<CategoryDto> results = categoryService.searchCategories(searchQuery);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(categoryDto.getId(), results.getFirst().getId());
        assertEquals(categoryDto.getName(), results.getFirst().getName());
        verify(categoryRepository).findByNameContainingIgnoreCase(searchQuery);
    }

    @Test
    void getAllCategories_ReturnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        List<CategoryDto> results = categoryService.getAllCategories();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(categoryDto.getId(), results.getFirst().getId());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategory_ExistingId_ReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = categoryService.getCategory(1L);

        assertNotNull(result);
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategory_NonExistingId_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategory(999L));
        verify(categoryRepository).findById(999L);
    }

    @Test
    void createCategory_ValidData_ReturnsCreatedCategory() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            CategoryDto result = categoryService.createCategory(1L, categoryDto);

            assertNotNull(result);
            assertEquals(categoryDto.getId(), result.getId());
            assertEquals(categoryDto.getName(), result.getName());
            verify(projectRepository).findById(1L);
            verify(categoryRepository).save(any(Category.class));
            securityUtils.verify(SecurityUtils::getCurrentUserId);
        }
    }

    @Test
    void createCategory_NonExistingProjectId_ThrowsException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> categoryService.createCategory(999L, categoryDto));
        verify(projectRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ExistingId_ReturnsUpdatedCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setDescription("Updated Description");

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_NonExistingId_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(999L, updateDto));
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ExistingId_DeletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_NonExistingId_ThrowsException() {
        // Given
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(999L));
        verify(categoryRepository).existsById(999L);
        verify(categoryRepository, never()).deleteById(any());
    }
} 