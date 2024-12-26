package local.pms.projectservice.mapping;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectMapping {
    ProjectMapping INSTANCE = Mappers.getMapper(ProjectMapping.class);
    ProjectDto toDto(Project project);
    Project toEntity(ProjectDto projectDto);
}
