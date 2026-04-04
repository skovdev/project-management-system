package local.pms.projectservice.service.impl;

import local.pms.projectservice.config.jwt.JwtTokenProvider;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.event.ProjectDeletedEvent;

import local.pms.projectservice.kafka.producer.ProjectDeletedProducer;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.provider.AiExternalProvider;

import local.pms.projectservice.mapping.ProjectMapping;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.TokenService;
import local.pms.projectservice.service.ProjectService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapping projectMapping = ProjectMapping.INSTANCE;

    private final ProjectRepository projectRepository;
    private final AiExternalProvider aiExternalProvider;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProjectDeletedProducer projectDeletedProducer;

    @Override
    @Transactional
    public ProjectDto create(ProjectDto projectDto) {
        if (projectDto == null) {
            log.error("ProjectDto is null, cannot create project.");
            throw new InvalidProjectInputException("Project data cannot be null. Please provide valid project information");
        }
        var project = projectMapping.toEntity(projectDto);
        project.setUserId(extractAuthUserId());
        var savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", savedProject.getId());
        return projectMapping.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAll(Pageable pageable) {
        return projectRepository.findAllByUserId(extractAuthUserId(), pageable)
                .map(projectMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto findById(UUID projectId) {
        var project = projectRepository.findByIdAndUserId(projectId, extractAuthUserId());
        if (project.isEmpty()) {
            log.error("Project with ID {} not found or access denied.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        return projectMapping.toDto(project.get());
    }

    @Override
    @Transactional
    public ProjectDto update(UUID projectId, ProjectDto projectDto) {
        if (projectDto == null) {
            log.error("ProjectDto is null, cannot update project.");
            throw new InvalidProjectInputException("Project data cannot be null. Please provide valid project information");
        }
        UUID authUserId = extractAuthUserId();
        var existingProject = projectRepository.findById(projectId);
        if (existingProject.isEmpty()) {
            log.error("Project with ID {} not found, cannot update.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        if (!existingProject.get().getUserId().equals(authUserId)) {
            log.error("User {} attempted to update project {} owned by another user.", authUserId, projectId);
            throw new ProjectAccessDeniedException("Access denied: you do not own project with ID " + projectId);
        }
        var projectToUpdate = existingProject.get();
        projectToUpdate.setTitle(projectDto.title());
        projectToUpdate.setDescription(projectDto.description());
        projectToUpdate.setProjectStatusType(projectDto.projectStatusType());
        projectToUpdate.setStartDate(projectDto.startDate());
        projectToUpdate.setEndDate(projectDto.endDate());
        var updatedProject = projectRepository.save(projectToUpdate);
        log.info("Project with ID {} updated successfully.", projectId);
        return projectMapping.toDto(updatedProject);
    }

    @Override
    @Transactional
    public void delete(UUID projectId) {
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            log.error("Project with ID {} not found, cannot delete.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        projectRepository.deleteById(projectId);
        log.info("Project with ID {} deleted successfully.", projectId);
        projectDeletedProducer.sendProjectDeletedEvent(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, new ProjectDeletedEvent(projectId));
    }

    @Override
    @Transactional
    public String generateProjectDescription(UUID projectId, String projectTitle) {
        UUID authUserId = extractAuthUserId();
        var project = projectRepository.findByIdAndUserId(projectId, authUserId);
        if (project.isEmpty()) {
            log.error("Project with ID {} not found or access denied for description generation.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        if (projectTitle == null || projectTitle.isBlank()) {
            log.error("Project title is null or blank, cannot generate description.");
            throw new InvalidProjectInputException("Project title cannot be null or blank. Please provide a valid project title");
        }
        try {
            log.info("Generating project description for title: {}", projectTitle);
            return aiExternalProvider.generateProjectDescription(projectTitle);
        } catch (Exception e) {
            log.error("Failed to generate project description for project title '{}': {}", projectTitle, e.getMessage());
            throw new DescriptionGenerationException("An error occurred while generating project description", e);
        }
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract auth user ID.");
            throw new ProjectAccessDeniedException("Authentication token is missing or invalid. Please provide a valid token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}
