package local.pms.aiservice.service;

/**
 * Domain service for task-related AI generation.
 * Owns the acceptance-criteria prompt and produces a formatted result
 * from the given task context supplied by the caller.
 */
public interface TaskAiService {

    /**
     * Generates acceptance criteria for a task.
     *
     * @param title       the task title
     * @param description the task description
     * @return AI-generated acceptance criteria text
     */
    String generateAcceptanceCriteria(String title, String description);
}
