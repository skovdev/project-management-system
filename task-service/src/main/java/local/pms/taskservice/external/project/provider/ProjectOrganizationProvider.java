package local.pms.taskservice.external.project.provider;

import java.util.UUID;

public interface ProjectOrganizationProvider {

    /**
     * Resolves the organization a project belongs to.
     *
     * @param projectId the project identifier
     * @return the organization identifier the project belongs to
     * @throws local.pms.taskservice.exception.TaskAccessDeniedException if the project
     *         could not be resolved (fails closed)
     */
    UUID resolveOrganizationId(UUID projectId);
}
