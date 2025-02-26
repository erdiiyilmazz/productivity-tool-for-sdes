package com.erdidev.taskmanager.service;

import com.erdidev.taskmanager.dto.ProjectDto;
import com.erdidev.taskmanager.exception.ProjectNotFoundException;
import com.erdidev.taskmanager.mapper.ProjectMapper;
import com.erdidev.taskmanager.model.Project;
import com.erdidev.taskmanager.repository.ProjectRepository;
import com.erdidev.common.util.SecurityUtils;
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
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjects(Pageable pageable) {
        log.debug("Fetching projects page: {}", pageable);
        return projectRepository.findAll(pageable)
                .map(projectMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProject(Long id) {
        log.debug("Fetching project with id: {}", id);
        return projectRepository.findById(id)
                .map(projectMapper::toDto)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Transactional
    public ProjectDto createProject(ProjectDto projectDto) {
        log.debug("Creating project: {}", projectDto);
        Project project = projectMapper.toEntity(projectDto);
        project.setOwnerId(SecurityUtils.getCurrentUserId());
        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        log.debug("Updating project with id: {}", id);
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        
        existingProject.setName(projectDto.getName());
        existingProject.setDescription(projectDto.getDescription());
        
        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        log.debug("Deleting project with id: {}", id);
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> searchProjects(String query) {
        log.debug("Searching projects with query: {}", query);
        return projectRepository.findByNameContainingIgnoreCase(query).stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }
} 