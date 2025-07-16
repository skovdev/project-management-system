package local.pms.projectservice.service.impl;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;

import local.pms.projectservice.client.aiservice.AiServiceClient;

import local.pms.projectservice.client.aiservice.promt.PromptMessage;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.mapping.ProjectMapping;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.ProjectService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    final ProjectMapping projectMapping = ProjectMapping.INSTANCE;

    final ProjectRepository projectRepository;
    final AiServiceClient aiServiceClient;

    @Override
    public Page<ProjectDto> findAll(int page, int size, String sortBy, String order) {
        return projectRepository.findAll(pageRequest(page, size, sortBy, order))
                .map(projectMapping::toDto);
    }

    @Override
    public String generateProjectDescription(UUID projectId, String projectTitle) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            log.error("Project with ID {} not found, cannot generate description.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        if (projectTitle == null || projectTitle.isBlank()) {
            log.error("Project title is null or blank, cannot generate description.");
            throw new InvalidProjectInputException("Project title cannot be null or blank. Please provide a valid project title");
        }
        try {
            log.info("Generating project description for title: {}", projectTitle);
            return aiServiceClient.generateProjectDescription(fillChatGptMessages(projectTitle));
        } catch (Exception e) {
            log.error("Failed to generate project description for project title '{}': {}", projectTitle, e.getMessage());
            throw new DescriptionGenerationException("An error occurred while generating project description", e);
        }
    }

    private List<ChatCompletionMessageParam> fillChatGptMessages(String projectTitle) {
        return List.of(
                ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam.builder()
                                .content(PromptMessage.SYSTEM_PROMPT_PROJECT_DESCRIPTION)
                                .build()
                ),
                ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder()
                                .content("Generate a project description for the following title: " + projectTitle)
                                .build()
                )
        );
    }

    private PageRequest pageRequest(int page, int size, String sortBy, String order) {
        return PageRequest.of(page, size, sorting(sortBy, order));
    }

    private Sort sorting(String sortBy, String order) {
        return Sort.by(Sort.Order.by(sortBy)
                .with(Sort.Direction.fromString(order)));
    }
}
