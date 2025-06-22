package local.pms.projectservice.service;

import local.pms.projectservice.dto.ProjectDto;

import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ProjectService {
    Page<ProjectDto> findAll(int page, int size, String sortBy, String order);
    String generateProjectDescription(UUID projectId, String projectTitle);
}
