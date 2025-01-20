package com.erdidev.timemanager.mapper;

import com.erdidev.timemanager.dto.ProjectDto;
import com.erdidev.timemanager.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(ProjectDto projectDto);

    ProjectDto toDto(Project project);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntity(ProjectDto projectDto, @MappingTarget Project project);
} 