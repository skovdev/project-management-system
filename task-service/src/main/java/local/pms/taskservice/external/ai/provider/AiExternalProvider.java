package local.pms.taskservice.external.ai.provider;

/**
 * Abstraction over the external AI provider used to generate task-related content.
 */
public interface AiExternalProvider {

    /**
     * Generates acceptance criteria for a task based on its title and description.
     *
     * @param taskTitle       the title of the task
     * @param taskDescription the description of the task
     * @return the AI-generated acceptance criteria text
     */
    String generateAcceptanceCriteria(String taskTitle, String taskDescription);
}
