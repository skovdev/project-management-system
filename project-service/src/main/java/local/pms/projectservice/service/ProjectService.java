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
     * Retrieves a paginated list of all projects belonging to an organization.
     * The caller must be a member of that organization.
     *
     * @param organizationId the organization identifier to scope the list to
     * @param pageable       pagination and sorting parameters
     * @return a page of project DTOs
     */
    Page<ProjectDto> findAll(UUID organizationId, Pageable pageable);

    /**
     * Finds a project by its identifier within an organization.
     * The caller must be a member of that organization.
     *
     * @param projectId      the unique project identifier
     * @param organizationId the organization the project must belong to
     * @return the project DTO
     */
    ProjectDto findById(UUID projectId, UUID organizationId);

    /**
     * Updates an existing project with new data.
     * The caller must be an OWNER or ADMIN member of the project's organization.
     *
     * @param projectId  the unique project identifier
     * @param projectDto the updated project data
     * @return the updated project DTO
     */
    ProjectDto update(UUID projectId, ProjectDto projectDto);

    /**
     * Soft-deletes a project by its identifier within an organization.
     * The caller must be an OWNER or ADMIN member of that organization.
     *
     * @param projectId      the unique project identifier
     * @param organizationId the organization the project must belong to
     */
    void delete(UUID projectId, UUID organizationId);

    /**
     * Generates an AI-powered description for a project.
     * The caller must be a member of the project's organization.
     *
     * @param projectId      the unique project identifier
     * @param organizationId the organization the project must belong to
     * @param projectTitle   the project title used as input for description generation
     * @return the generated project description
     */
    String generateProjectDescription(UUID projectId, UUID organizationId, String projectTitle);

    /**
     * Deletes all projects belonging to the specified organization.
     * Invoked as part of the cascade delete flow when an organization is removed.
     *
     * @param organizationId the UUID of the deleted organization
     */
    void deleteAllByOrganizationId(UUID organizationId);

    /**
     * Resolves the organization a project belongs to. Used by other services
     * (e.g. task-service) to derive organizationId from a projectId.
     * The caller must be a member of the resolved organization.
     *
     * @param projectId the unique project identifier
     * @return the organization identifier the project belongs to
     * @throws local.pms.projectservice.exception.ProjectAccessDeniedException if the caller
     *         is not a member of the project's organization
     */
    UUID findOrganizationIdByProjectId(UUID projectId);
}
