package local.pms.aiservice.service;

/**
 * Domain service for project-related AI generation.
 * Owns the project-description prompt and produces a formatted result
 * from the given project title supplied by the caller.
 */
public interface ProjectAiService {

    /**
     * Generates a project description from a title.
     *
     * @param title the project title
     * @return AI-generated project description text
     */
    String generateProjectDescription(String title);
}
