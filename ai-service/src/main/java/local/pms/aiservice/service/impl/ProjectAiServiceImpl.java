package local.pms.aiservice.service.impl;

import local.pms.aiservice.prompt.ProjectPrompts;

import local.pms.aiservice.service.AiChatService;
import local.pms.aiservice.service.ProjectAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Implementation of {@link ProjectAiService} that composes the project-description prompt
 * and delegates the actual model call to {@link AiChatService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectAiServiceImpl implements ProjectAiService {

    private final AiChatService aiChatService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateProjectDescription(String title) {
        log.info("Generating project description for title='{}'", title);
        var userPrompt = "Generate a project description for the following title: " + title;
        return aiChatService.chat(ProjectPrompts.SYSTEM_PROMPT_PROJECT_DESCRIPTION, userPrompt);
    }
}
