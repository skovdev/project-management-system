package local.pms.aiservice.service.impl;

import local.pms.aiservice.prompt.TaskPrompts;

import local.pms.aiservice.service.AiChatService;
import local.pms.aiservice.service.TaskAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

/**
 * Implementation of {@link TaskAiService} that composes the acceptance-criteria and
 * comment-suggestions prompts and delegates the actual model call to {@link AiChatService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAiServiceImpl implements TaskAiService {

    private final AiChatService aiChatService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateAcceptanceCriteria(String title, String description) {
        log.info("Generating acceptance criteria for task title='{}'", title);
        var userPrompt = "Task title: " + title + "\nTask description: " + description;
        return aiChatService.chat(TaskPrompts.SYSTEM_PROMPT_ACCEPTANCE_CRITERIA, userPrompt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> generateCommentSuggestions(String taskTitle, String taskDescription, String commentContent, List<String> threadContext) {
        log.info("Generating comment reply suggestions for task title='{}'", taskTitle);
        var userPrompt = buildCommentSuggestionsPrompt(taskTitle, taskDescription, commentContent, threadContext);
        var response = aiChatService.chat(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS, userPrompt);
        return parseSuggestions(response);
    }

    private String buildCommentSuggestionsPrompt(String taskTitle, String taskDescription, String commentContent, List<String> threadContext) {
        var prompt = new StringBuilder()
                .append("Task title: ").append(taskTitle).append("\n")
                .append("Task description: ").append(taskDescription).append("\n");
        if (threadContext == null || threadContext.isEmpty()) {
            prompt.append("Thread context: none\n");
        } else {
            prompt.append("Thread context (previous comments, most recent first):\n");
            for (int i = 0; i < threadContext.size(); i++) {
                prompt.append(i + 1).append(". ").append(threadContext.get(i)).append("\n");
            }
        }
        prompt.append("Comment to reply to: ").append(commentContent);
        return prompt.toString();
    }

    /**
     * Parses the model's numbered-list response into individual suggestion strings,
     * stripping any leading numbering (e.g. {@code "1. "} or {@code "1) "}).
     */
    private List<String> parseSuggestions(String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .map(line -> line.replaceFirst("^\\d+[.)]\\s*", ""))
                .filter(line -> !line.isBlank())
                .toList();
    }
}
