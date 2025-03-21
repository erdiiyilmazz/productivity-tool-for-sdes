package com.erdidev.taskmanager.mapper;

import com.erdidev.taskmanager.dto.ProjectDto;
import com.erdidev.taskmanager.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(ProjectDto projectDto);

    ProjectDto toDto(Project project);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntity(ProjectDto projectDto, @MappingTarget Project project);
} 