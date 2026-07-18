package local.pms.projectservice.mapping;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectDto toDto(Project project);
    Project toEntity(ProjectDto projectDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(ProjectDto projectDto, @MappingTarget Project project);
}
