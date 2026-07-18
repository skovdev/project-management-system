package local.pms.projectservice.service;

import local.pms.projectservice.config.jwt.JwtTokenProvider;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.event.ProjectCreatedEvent;
import local.pms.projectservice.event.ProjectDeletedEvent;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.provider.AiExternalProvider;

import local.pms.projectservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.impl.ProjectServiceImpl;

import local.pms.projectservice.type.ProjectStatusType;
import local.pms.projectservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

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
    private OrganizationAccessProvider organizationAccessProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("create saves project and returns DTO when caller is OWNER of the organization")
    void should_saveAndReturnDto_when_createWithValidDataAndOwnerRole() {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var dto = buildProjectDto(null, null, organizationId);
        var saved = buildProject(UUID.randomUUID(), userId, organizationId);

        stubToken(userId);
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.OWNER);
        when(projectRepository.save(any(Project.class))).thenReturn(saved);

        var result = projectService.create(dto);

        assertThat(result.title()).isEqualTo("My Project");
        verify(projectRepository).save(any(Project.class));
        verify(eventPublisher).publishEvent(any(ProjectCreatedEvent.class));
    }

    @Test
    @DisplayName("create throws ProjectAccessDeniedException when caller is only a MEMBER")
    void should_throwAccessDenied_when_createWithMemberRole() {
        var organizationId = UUID.randomUUID();
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        assertThatThrownBy(() -> projectService.create(buildProjectDto(null, null, organizationId)))
                .isInstanceOf(ProjectAccessDeniedException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("create propagates ProjectAccessDeniedException when membership verification fails closed")
    void should_propagateAccessDenied_when_membershipVerificationFails() {
        var organizationId = UUID.randomUUID();
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new ProjectAccessDeniedException("Access denied: unable to verify membership"));

        assertThatThrownBy(() -> projectService.create(buildProjectDto(null, null, organizationId)))
                .isInstanceOf(ProjectAccessDeniedException.class);

        verify(projectRepository, never()).save(any());
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
    @DisplayName("findAll returns page of DTOs scoped to the given organization")
    void should_returnPageOfDtos_when_findAll() {
        var organizationId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var project = buildProject(UUID.randomUUID(), UUID.randomUUID(), organizationId);
        var page = new PageImpl<>(List.of(project), pageable, 1);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findAllByOrganizationId(organizationId, pageable)).thenReturn(page);

        var result = projectService.findAll(organizationId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findById returns DTO when project exists in the organization")
    void should_returnDto_when_findByIdExists() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID(), organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));

        var result = projectService.findById(projectId, organizationId);

        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.title()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findById throws ProjectNotFoundException when project not found in the organization")
    void should_throwProjectNotFoundException_when_findByIdNotFound() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(projectId, organizationId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());
    }

    @Test
    @DisplayName("update returns updated DTO when caller is ADMIN of the organization")
    void should_returnUpdatedDto_when_callerIsAdmin() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var existing = buildProject(projectId, UUID.randomUUID(), organizationId);
        var updateDto = new ProjectDto(projectId, "Updated Title", "Updated Desc",
                ProjectStatusType.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now().plusDays(10), null, organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.ADMIN);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(existing));
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
    @DisplayName("update throws ProjectNotFoundException when project not found in the organization")
    void should_throwProjectNotFoundException_when_updateNotFound() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.OWNER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(projectId, buildProjectDto(projectId, null, organizationId)))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws ProjectAccessDeniedException when caller is only a MEMBER")
    void should_throwProjectAccessDeniedException_when_callerIsMember() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        assertThatThrownBy(() -> projectService.update(projectId, buildProjectDto(projectId, null, organizationId)))
                .isInstanceOf(ProjectAccessDeniedException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes project and publishes ProjectDeletedEvent when caller is OWNER")
    void should_deleteAndPublishEvent_when_projectExists() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID(), organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.OWNER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));

        projectService.delete(projectId, organizationId);

        verify(projectRepository).deleteById(projectId);
        verify(eventPublisher).publishEvent(any(ProjectDeletedEvent.class));
    }

    @Test
    @DisplayName("delete throws ProjectNotFoundException when project not found in the organization")
    void should_throwProjectNotFoundException_when_deleteNotFound() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.OWNER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(projectId, organizationId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any(ProjectDeletedEvent.class));
    }

    @Test
    @DisplayName("delete throws ProjectAccessDeniedException when caller is only a MEMBER")
    void should_throwAccessDenied_when_deleteWithMemberRole() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        assertThatThrownBy(() -> projectService.delete(projectId, organizationId))
                .isInstanceOf(ProjectAccessDeniedException.class);

        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("generateProjectDescription returns AI-generated description")
    void should_returnDescription_when_generateDescriptionSucceeds() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID(), organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(aiExternalProvider.generateProjectDescription("My Project")).thenReturn("Great project!");

        var result = projectService.generateProjectDescription(projectId, organizationId, "My Project");

        assertThat(result).isEqualTo("Great project!");
    }

    @Test
    @DisplayName("generateProjectDescription throws ProjectNotFoundException when project not found")
    void should_throwProjectNotFoundException_when_generateDescriptionProjectNotFound() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, organizationId, "My Project"))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId.toString());
    }

    @Test
    @DisplayName("generateProjectDescription throws InvalidProjectInputException when title is blank")
    void should_throwInvalidProjectInputException_when_titleIsBlank() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID(), organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, organizationId, "  "))
                .isInstanceOf(InvalidProjectInputException.class)
                .hasMessageContaining("title cannot be null or blank");
    }

    @Test
    @DisplayName("generateProjectDescription throws DescriptionGenerationException when AI fails")
    void should_throwDescriptionGenerationException_when_aiFails() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var project = buildProject(projectId, UUID.randomUUID(), organizationId);

        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(aiExternalProvider.generateProjectDescription("My Project"))
                .thenThrow(new RuntimeException("AI unavailable"));

        assertThatThrownBy(() -> projectService.generateProjectDescription(projectId, organizationId, "My Project"))
                .isInstanceOf(DescriptionGenerationException.class);
    }

    @Test
    @DisplayName("create throws ProjectAccessDeniedException when token is null")
    void should_throwProjectAccessDeniedException_when_tokenIsNull() {
        var organizationId = UUID.randomUUID();
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.OWNER);
        when(tokenService.getToken()).thenReturn(null);

        assertThatThrownBy(() -> projectService.create(buildProjectDto(null, null, organizationId)))
                .isInstanceOf(ProjectAccessDeniedException.class)
                .hasMessageContaining("Authentication token is missing");
    }

    @Test
    @DisplayName("deleteAllByOrganizationId bulk soft-deletes and publishes an event for each project in the organization")
    void should_deleteAndPublishEventPerProject_when_deleteAllByOrganizationId() {
        var organizationId = UUID.randomUUID();
        var projectId1 = UUID.randomUUID();
        var projectId2 = UUID.randomUUID();

        when(projectRepository.findIdByOrganizationId(organizationId)).thenReturn(List.of(projectId1, projectId2));

        projectService.deleteAllByOrganizationId(organizationId);

        verify(projectRepository).softDeleteAllByOrganizationId(organizationId);
        verify(projectRepository, never()).deleteById(any());
        verify(eventPublisher).publishEvent(new ProjectDeletedEvent(projectId1));
        verify(eventPublisher).publishEvent(new ProjectDeletedEvent(projectId2));
    }

    @Test
    @DisplayName("deleteAllByOrganizationId publishes no events when the organization has no projects")
    void should_doNothing_when_deleteAllByOrganizationIdWithNoProjects() {
        var organizationId = UUID.randomUUID();
        when(projectRepository.findIdByOrganizationId(organizationId)).thenReturn(List.of());

        projectService.deleteAllByOrganizationId(organizationId);

        verify(projectRepository).softDeleteAllByOrganizationId(organizationId);
        verify(eventPublisher, never()).publishEvent(any(ProjectDeletedEvent.class));
    }

    private void stubToken(UUID userId) {
        when(tokenService.getToken()).thenReturn("test-token");
        when(jwtTokenProvider.extractAuthUserId("test-token")).thenReturn(userId);
    }

    private Project buildProject(UUID id, UUID userId, UUID organizationId) {
        var project = new Project();
        project.setId(id);
        project.setTitle("My Project");
        project.setDescription("A project description");
        project.setProjectStatusType(ProjectStatusType.PLANNING);
        project.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2026, 12, 31, 0, 0));
        project.setUserId(userId);
        project.setOrganizationId(organizationId);
        project.setDeleted(false);
        return project;
    }

    private ProjectDto buildProjectDto(UUID id, UUID userId, UUID organizationId) {
        return new ProjectDto(
                id,
                "My Project",
                "A project description",
                ProjectStatusType.PLANNING,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 0, 0),
                userId,
                organizationId
        );
    }
}
