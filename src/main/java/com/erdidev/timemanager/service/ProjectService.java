package com.erdidev.timemanager.service;

import com.erdidev.timemanager.dto.ProjectDto;
import com.erdidev.timemanager.exception.ProjectNotFoundException;
import com.erdidev.timemanager.mapper.ProjectMapper;
import com.erdidev.timemanager.model.Project;
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
        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        log.debug("Updating project with id: {}", id);
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        
        projectMapper.updateEntity(projectDto, existingProject);
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