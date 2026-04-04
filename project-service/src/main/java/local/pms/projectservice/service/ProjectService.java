package local.pms.projectservice.service;

import local.pms.projectservice.dto.ProjectDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for project management operations.
 */
public interface ProjectService {

    /**
     * Creates a new project.
     *
     * @param projectDto the project data to create
     * @return the created project DTO
     */
    ProjectDto create(ProjectDto projectDto);

    /**
     * Retrieves a paginated list of all projects.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of project DTOs
     */
    Page<ProjectDto> findAll(Pageable pageable);

    /**
     * Finds a project by its identifier.
     *
     * @param projectId the unique project identifier
     * @return the project DTO
     */
    ProjectDto findById(UUID projectId);

    /**
     * Updates an existing project with new data.
     *
     * @param projectId  the unique project identifier
     * @param projectDto the updated project data
     * @return the updated project DTO
     */
    ProjectDto update(UUID projectId, ProjectDto projectDto);

    /**
     * Soft-deletes a project by its identifier.
     *
     * @param projectId the unique project identifier
     */
    void delete(UUID projectId);

    /**
     * Generates an AI-powered description for a project.
     *
     * @param projectId    the unique project identifier
     * @param projectTitle the project title used as input for description generation
     * @return the generated project description
     */
    String generateProjectDescription(UUID projectId, String projectTitle);
}
