package local.pms.projectservice.service.impl;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.provider.AiExternalProvider;

import local.pms.projectservice.mapping.ProjectMapping;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.ProjectService;

import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapping projectMapping = ProjectMapping.INSTANCE;

    private final ProjectRepository projectRepository;
    private final AiExternalProvider aiExternalProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(projectMapping::toDto);
    }

    @Override
    @Transactional
    public ProjectDto create(ProjectDto projectDto) {
        if (projectDto == null) {
            log.error("ProjectDto is null, cannot create project.");
            throw new InvalidProjectInputException("Project data cannot be null. Please provide valid project information");
        }
        Project project = projectMapping.toEntity(projectDto);
        Project savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", savedProject.getId());
        return projectMapping.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto findById(UUID projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            log.error("Project with ID {} not found.", projectId);
            throw new ProjectNotFoundException("Project with ID " + projectId + " not found. Please provide a valid project ID");
        }
        return projectMapping.toDto(project.get());
    }

    @Override
    @Transactional
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
            return aiExternalProvider.generateProjectDescription(projectTitle);
        } catch (Exception e) {
            log.error("Failed to generate project description for project title '{}': {}", projectTitle, e.getMessage());
            throw new DescriptionGenerationException("An error occurred while generating project description", e);
        }
    }
}
