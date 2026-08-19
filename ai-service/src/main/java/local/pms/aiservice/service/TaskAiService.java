package local.pms.aiservice.service;

import java.util.List;

/**
 * Domain service for task-related AI generation.
 * Owns the acceptance-criteria and comment-suggestions prompts and produces a formatted
 * result from the given task context supplied by the caller.
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

    /**
     * Generates 3 reply suggestions for a task comment in a single AI request.
     *
     * @param taskTitle       the title of the task the comment belongs to
     * @param taskDescription the description of the task the comment belongs to
     * @param commentContent  the content of the comment to reply to
     * @param threadContext   recent prior comments on the same task, used as additional context; may be null or empty
     * @return exactly 3 AI-generated reply suggestions
     */
    List<String> generateCommentSuggestions(String taskTitle, String taskDescription, String commentContent, List<String> threadContext);
}
