package local.pms.projectservice.service;

import local.pms.projectservice.dto.ProjectDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {
    ProjectDto create(ProjectDto projectDto);
    Page<ProjectDto> findAll(Pageable pageable);
    ProjectDto findById(UUID projectId);
    String generateProjectDescription(UUID projectId, String projectTitle);
}
