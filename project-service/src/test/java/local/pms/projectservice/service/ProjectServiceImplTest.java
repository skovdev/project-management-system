package local.pms.projectservice.service;

import local.pms.projectservice.config.jwt.JwtTokenProvider;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.provider.AiExternalProvider;

import local.pms.projectservice.kafka.producer.ProjectDeletedProducer;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.impl.ProjectServiceImpl;

import local.pms.projectservice.type.ProjectStatusType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AiExternalProvider aiExternalProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ProjectDeletedProducer projectDeletedProducer;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("create saves project and returns DTO with userId from token")
    void should_saveAndReturnDto_when_createWithValidData() {
        var userId = UUID.randomUUID();
        var dto = buildProjectDto(null, null);
        var saved = buildProject(UUID.randomUUID(), userId);

        stubToken(userId);
        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        var result = projectService.create(dto);

        assertThat(result.title()).isEqualTo("My Project");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("create throws InvalidProjectInputException when DTO is null")
    void should_throwInvalidProjectInputException_when_createWithNullDto() {
        assertThatThrownBy(() -> projectService.create(null))
                .isInstanceOf(InvalidProjectInputException.class)
                .hasMessageContaining("cannot be null");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll returns page of DTOs filtered by userId from token")
    void should_returnPageOfDtos_when_findAll() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var project = buildProject(UUID.randomUUID(), userId);
        var page = new PageImpl<>(List.of(project), pageable, 1);

        stubToken(userId);
        when(projectRepository.findAllByUserId(userId, pageable)).thenReturn(page);

        var result = projectService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findById returns DTO when project exists for userId")
    void should_returnDto_when_findByIdExists() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var project = buildProject(projectId, userId);

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));

        var result = projectService.findById(projectId);

        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.title()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findById throws ProjectNotFoundException when project not found")
    void should_throwProjectNotFoundException_when_findByIdNotFound() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());
    }

    @Test
    @DisplayName("update returns updated DTO when caller owns the project")
    void should_returnUpdatedDto_when_callerOwnsProject() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var existing = buildProject(projectId, userId);
        var updateDto = new ProjectDto(projectId, "Updated Title", "Updated Desc",
                ProjectStatusType.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now().plusDays(10), null);

        stubToken(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        var result = projectService.update(projectId, updateDto);

        assertThat(result).isNotNull();
        verify(projectRepository).save(existing);
    }

    @Test
    @DisplayName("update throws InvalidProjectInputException when DTO is null")
    void should_throwInvalidProjectInputException_when_updateWithNullDto() {
        assertThatThrownBy(() -> projectService.update(UUID.randomUUID(), null))
                .isInstanceOf(InvalidProjectInputException.class)
                .hasMessageContaining("cannot be null");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws ProjectNotFoundException when project not found")
    void should_throwProjectNotFoundException_when_updateNotFound() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        stubToken(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(projectId, buildProjectDto(projectId, null)))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws ProjectAccessDeniedException when caller does not own the project")
    void should_throwProjectAccessDeniedException_when_callerNotOwner() {
        var projectId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var existing = buildProject(projectId, ownerId);

        stubToken(callerId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> projectService.update(projectId, buildProjectDto(projectId, null)))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes project and publishes ProjectDeletedEvent")
    void should_deleteAndPublishEvent_when_projectExists() {
        var projectId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.delete(projectId);

        verify(projectRepository).deleteById(projectId);
        verify(projectDeletedProducer).sendProjectDeletedEvent(anyString(), any());
    }

    @Test
    @DisplayName("delete throws ProjectNotFoundException when project not found")
    void should_throwProjectNotFoundException_when_deleteNotFound() {
        var projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).deleteById(any());
        verify(projectDeletedProducer, never()).sendProjectDeletedEvent(any(), any());
    }

    @Test
    @DisplayName("generateProjectDescription returns AI-generated description")
    void should_returnDescription_when_generateDescriptionSucceeds() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var project = buildProject(projectId, userId);

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(aiExternalProvider.generateProjectDescription("My Project")).thenReturn("Great project!");

        var result = projectService.generateProjectDescription(projectId, "My Project");

        assertThat(result).isEqualTo("Great project!");
    }

    @Test
    @DisplayName("generateProjectDescription throws ProjectNotFoundException when project not found")
    void should_throwProjectNotFoundException_when_generateDescriptionProjectNotFound() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, "My Project"))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());
    }

    @Test
    @DisplayName("generateProjectDescription throws InvalidProjectInputException when title is blank")
    void should_throwInvalidProjectInputException_when_titleIsBlank() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var project = buildProject(projectId, userId);

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, "  "))
                .isInstanceOf(InvalidProjectInputException.class)
                .hasMessageContaining("title cannot be null or blank");
    }

    @Test
    @DisplayName("generateProjectDescription throws DescriptionGenerationException when AI fails")
    void should_throwDescriptionGenerationException_when_aiFails() {
        var projectId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var project = buildProject(projectId, userId);

        stubToken(userId);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(aiExternalProvider.generateProjectDescription("My Project"))
                .thenThrow(new RuntimeException("AI unavailable"));

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, "My Project"))
                .isInstanceOf(DescriptionGenerationException.class);
    }

    @Test
    @DisplayName("create throws ProjectAccessDeniedException when token is null")
    void should_throwProjectAccessDeniedException_when_tokenIsNull() {
        when(tokenService.getToken()).thenReturn(null);

        assertThatThrownBy(() -> projectService.create(buildProjectDto(null, null)))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessageContaining("Authentication token is missing");
    }

    private void stubToken(UUID userId) {
        when(tokenService.getToken()).thenReturn("test-token");
        when(jwtTokenProvider.extractAuthUserId("test-token")).thenReturn(userId);
    }

    private Project buildProject(UUID id, UUID userId) {
        var project = new Project();
        project.setId(id);
        project.setTitle("My Project");
        project.setDescription("A project description");
        project.setProjectStatusType(ProjectStatusType.PLANNING);
        project.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2026, 12, 31, 0, 0));
        project.setUserId(userId);
        project.setDeleted(false);
        return project;
    }

    private ProjectDto buildProjectDto(UUID id, UUID userId) {
        return new ProjectDto(
                id,
                "My Project",
                "A project description",
                ProjectStatusType.PLANNING,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 0, 0),
                userId
        );
    }
}
