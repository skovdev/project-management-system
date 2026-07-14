package local.pms.projectservice.service.impl;

import local.pms.projectservice.config.jwt.JwtTokenProvider;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.event.ProjectCreatedEvent;
import local.pms.projectservice.event.ProjectDeletedEvent;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.provider.AiExternalProvider;

import local.pms.projectservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.projectservice.mapping.ProjectMapping;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.TokenService;
import local.pms.projectservice.service.ProjectService;

import local.pms.projectservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;

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
    private final OrganizationAccessProvider organizationAccessProvider;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProjectDto create(ProjectDto projectDto) {
        if (projectDto == null) {
            log.error("ProjectDto is null, cannot create project.");
            throw new InvalidProjectInputException("Project data cannot be null. Please provide valid project information");
        }
        requireCreatorRole(projectDto.organizationId());

        var project = projectMapping.toEntity(projectDto);
        project.setUserId(extractAuthUserId());
        project.setOrganizationId(projectDto.organizationId());
        var savedProject = projectRepository.save(project);
        log.info("Project created with ID: {} in organization: {}", savedProject.getId(), savedProject.getOrganizationId());
        eventPublisher.publishEvent(
                new ProjectCreatedEvent(savedProject.getId(), savedProject.getUserId(), savedProject.getTitle()));
        return projectMapping.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAll(UUID organizationId, Pageable pageable) {
        organizationAccessProvider.verifyMembership(organizationId);
        return projectRepository.findAllByOrganizationId(organizationId, pageable)
                .map(projectMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto findById(UUID projectId, UUID organizationId) {
        organizationAccessProvider.verifyMembership(organizationId);
        return projectMapping.toDto(findProjectOrThrow(projectId, organizationId));
    }

    @Override
    @Transactional
    public ProjectDto update(UUID projectId, ProjectDto projectDto) {
        if (projectDto == null) {
            log.error("ProjectDto is null, cannot update project.");
            throw new InvalidProjectInputException("Project data cannot be null. Please provide valid project information");
        }
        requireCreatorRole(projectDto.organizationId());

        var projectToUpdate = findProjectOrThrow(projectId, projectDto.organizationId());
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
    public void delete(UUID projectId, UUID organizationId) {
        requireCreatorRole(organizationId);

        findProjectOrThrow(projectId, organizationId);
        projectRepository.deleteById(projectId);
        log.info("Project with ID {} deleted successfully.", projectId);
        eventPublisher.publishEvent(new ProjectDeletedEvent(projectId));
    }

    @Override
    @Transactional
    public String generateProjectDescription(UUID projectId, UUID organizationId, String projectTitle) {
        organizationAccessProvider.verifyMembership(organizationId);
        findProjectOrThrow(projectId, organizationId);
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

    @Override
    @Transactional
    public void deleteAllByOrganizationId(UUID organizationId) {
        log.info("Deleting all projects for organizationId: {}", organizationId);
        var projects = projectRepository.findAllByOrganizationId(organizationId, Pageable.unpaged());
        projects.forEach(project -> {
            projectRepository.deleteById(project.getId());
            eventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        });
        log.info("All projects deleted for organizationId: {}", organizationId);
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract auth user ID.");
            throw new ProjectAccessDeniedException("Authentication token is missing or invalid. Please provide a valid token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }

    private void requireCreatorRole(UUID organizationId) {
        var role = organizationAccessProvider.verifyMembership(organizationId);
        if (role != OrganizationRoleType.OWNER && role != OrganizationRoleType.ADMIN) {
            log.error("Caller with role {} attempted a restricted action on organization {}.", role, organizationId);
            throw new ProjectAccessDeniedException(
                    "Access denied: you do not have sufficient permissions in organization with ID " + organizationId);
        }
    }

    private Project findProjectOrThrow(UUID projectId, UUID organizationId) {
        return projectRepository.findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> {
                    log.error("Project with ID {} not found in organization {}.", projectId, organizationId);
                    return new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
                });
    }
}
