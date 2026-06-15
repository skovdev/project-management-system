package local.pms.aiservice.service.impl;

import local.pms.aiservice.prompt.TaskPrompts;

import local.pms.aiservice.service.AiChatService;
import local.pms.aiservice.service.TaskAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Implementation of {@link TaskAiService} that composes the acceptance-criteria prompt
 * and delegates the actual model call to {@link AiChatService}.
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
}
