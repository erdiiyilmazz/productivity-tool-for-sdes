package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.ProjectDto;
import com.erdidev.taskmanager.exception.ProjectNotFoundException;
import com.erdidev.taskmanager.mapper.ProjectMapper;
import com.erdidev.taskmanager.model.Project;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectDto projectDto;
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // Set up test project
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwnerId(testUserId);

        // Set up project DTO
        projectDto = new ProjectDto();
        projectDto.setId(1L);
        projectDto.setName("Test Project");
        projectDto.setDescription("Test Description");
        // The DTO might not have ownerId setter as it's typically not exposed
    }

    @Test
    void getProjects_ReturnsPageOfProjects() {
        // Given
        Pageable pageable = mock(Pageable.class);
        Page<Project> projectPage = new PageImpl<>(List.of(project));
        
        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
        when(projectMapper.toDto(project)).thenReturn(projectDto);

        // When
        Page<ProjectDto> result = projectService.getProjects(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(projectDto.getId(), result.getContent().get(0).getId());
        assertEquals(projectDto.getName(), result.getContent().get(0).getName());
        verify(projectRepository).findAll(pageable);
    }

    @Test
    void getProject_ExistingId_ReturnsProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectDto);

        // When
        ProjectDto result = projectService.getProject(1L);

        // Then
        assertNotNull(result);
        assertEquals(projectDto.getId(), result.getId());
        assertEquals(projectDto.getName(), result.getName());
        verify(projectRepository).findById(1L);
    }

    @Test
    void getProject_NonExistingId_ThrowsException() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProjectNotFoundException.class, () -> projectService.getProject(999L));
        verify(projectRepository).findById(999L);
    }

    @Test
    void createProject_ValidData_ReturnsCreatedProject() {
        // Given
        when(projectMapper.toEntity(projectDto)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectDto);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            // When
            ProjectDto result = projectService.createProject(projectDto);

            // Then
            assertNotNull(result);
            assertEquals(projectDto.getId(), result.getId());
            assertEquals(projectDto.getName(), result.getName());
            verify(projectRepository).save(any(Project.class));
            securityUtils.verify(SecurityUtils::getCurrentUserId);
        }
    }

    @Test
    void updateProject_ExistingId_ReturnsUpdatedProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(projectDto);

        ProjectDto updateDto = new ProjectDto();
        updateDto.setName("Updated Project");
        updateDto.setDescription("Updated Description");

        // When
        ProjectDto result = projectService.updateProject(1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
        assertEquals(projectDto.getName(), result.getName());
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_NonExistingId_ThrowsException() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ProjectDto updateDto = new ProjectDto();
        updateDto.setName("Updated Project");

        // When & Then
        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(999L, updateDto));
        verify(projectRepository).findById(999L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_ExistingId_DeletesProject() {
        // Given
        when(projectRepository.existsById(1L)).thenReturn(true);
        doNothing().when(projectRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> projectService.deleteProject(1L));

        // Then
        verify(projectRepository).existsById(1L);
        verify(projectRepository).deleteById(1L);
    }

    @Test
    void deleteProject_NonExistingId_ThrowsException() {
        // Given
        when(projectRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ProjectNotFoundException.class, () -> projectService.deleteProject(999L));
        verify(projectRepository).existsById(999L);
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void searchProjects_ReturnsMatchingProjects() {
        // Given
        String searchQuery = "test";
        when(projectRepository.findByNameContainingIgnoreCase(searchQuery)).thenReturn(List.of(project));
        when(projectMapper.toDto(project)).thenReturn(projectDto);

        // When
        List<ProjectDto> results = projectService.searchProjects(searchQuery);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(projectDto.getId(), results.get(0).getId());
        assertEquals(projectDto.getName(), results.get(0).getName());
        verify(projectRepository).findByNameContainingIgnoreCase(searchQuery);
    }
} 