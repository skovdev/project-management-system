package local.pms.taskservice.external.ai.provider;

import java.util.List;

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

    /**
     * Generates 3 reply suggestions for a task comment.
     *
     * @param taskTitle       the title of the task the comment belongs to
     * @param taskDescription the description of the task the comment belongs to
     * @param commentContent  the content of the comment to reply to
     * @param threadContext   recent prior comments on the same task, used as additional context
     * @return exactly 3 AI-generated reply suggestions
     */
    List<String> generateCommentSuggestions(String taskTitle, String taskDescription, String commentContent, List<String> threadContext);
}
