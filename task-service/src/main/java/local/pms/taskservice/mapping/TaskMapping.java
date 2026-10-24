package local.pms.taskservice.mapping;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.entity.Task;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapping {
    TaskMapping INSTANCE = Mappers.getMapper(TaskMapping.class);
    TaskDto toDto(Task task);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "assigneeId", ignore = true)
    Task toEntity(TaskDto taskDto);
}