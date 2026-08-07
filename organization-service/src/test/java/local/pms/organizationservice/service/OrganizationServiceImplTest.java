package local.pms.organizationservice.service;

import local.pms.organizationservice.dto.OrganizationDto;

import local.pms.organizationservice.entity.Organization;
import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;

import local.pms.organizationservice.repository.OrganizationRepository;
import local.pms.organizationservice.repository.OrganizationMemberRepository;

import local.pms.organizationservice.service.impl.OrganizationAccessGuard;
import local.pms.organizationservice.service.impl.OrganizationServiceImpl;

import local.pms.organizationservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private OrganizationAccessGuard organizationAccessGuard;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    @Test
    @DisplayName("create saves organization and adds caller as OWNER member")
    void should_saveOrganizationAndOwnerMembership_when_create() {
        var authUserId = UUID.randomUUID();
        var dto = buildOrganizationDto(null, null);
        var saved = buildOrganization(UUID.randomUUID(), authUserId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationRepository.save(any(Organization.class))).thenReturn(saved);

        var result = organizationService.create(dto);

        assertThat(result.name()).isEqualTo("My Organization");
        verify(organizationRepository).save(any(Organization.class));
        verify(organizationMemberRepository).save(any(OrganizationMember.class));
    }

    @Test
    @DisplayName("findAll returns page of organizations the caller belongs to")
    void should_returnPageOfDtos_when_findAll() {
        var authUserId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var organization = buildOrganization(UUID.randomUUID(), authUserId);
        var page = new PageImpl<>(List.of(organization), pageable, 1);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationRepository.findAllByMemberUserId(authUserId, pageable)).thenReturn(page);

        var result = organizationService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("My Organization");
    }

    @Test
    @DisplayName("findById returns DTO when caller is a member")
    void should_returnDto_when_findByIdExists() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, authUserId);
        var member = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

        var result = organizationService.findById(organizationId);

        assertThat(result.id()).isEqualTo(organizationId);
        assertThat(result.name()).isEqualTo("My Organization");
    }

    @Test
    @DisplayName("findById propagates OrganizationNotFoundException when caller is not a member")
    void should_propagateNotFound_when_findByIdNotAMember() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + organizationId + " not found"));

        assertThatThrownBy(() -> organizationService.findById(organizationId))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessageContaining(organizationId.toString());
    }

    @Test
    @DisplayName("update returns updated DTO when caller is OWNER")
    void should_returnUpdatedDto_when_callerIsOwner() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, authUserId);
        var member = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var updateDto = new OrganizationDto(organizationId, "Updated Name", "Updated Desc", null);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(organization)).thenReturn(organization);

        var result = organizationService.update(organizationId, updateDto);

        assertThat(result).isNotNull();
        verify(organizationRepository).save(organization);
    }

    @Test
    @DisplayName("update throws OrganizationAccessDeniedException when caller lacks permission")
    void should_throwAccessDenied_when_updateWithoutPermission() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, UUID.randomUUID());
        var member = buildMember(organization, authUserId, OrganizationRoleType.MEMBER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        doThrow(new OrganizationAccessDeniedException("Access denied"))
                .when(organizationAccessGuard).requireAtLeast(organizationId, member, OrganizationRoleType.ADMIN);

        assertThatThrownBy(() -> organizationService.update(organizationId, buildOrganizationDto(organizationId, null)))
                .isInstanceOf(OrganizationAccessDeniedException.class);

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws OrganizationNotFoundException when organization missing after membership check")
    void should_throwNotFound_when_updateOrganizationMissing() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, authUserId);
        var member = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.update(organizationId, buildOrganizationDto(organizationId, null)))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    @DisplayName("delete removes organization when caller is OWNER")
    void should_deleteOrganization_when_callerIsOwner() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, authUserId);
        var member = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

        organizationService.delete(organizationId);

        verify(organizationMemberRepository).deleteAllByOrganizationId(organizationId);
        verify(organizationRepository).deleteById(organizationId);
        verify(eventPublisher).publishEvent(any(OrganizationDeletedEvent.class));
    }

    @Test
    @DisplayName("delete throws OrganizationAccessDeniedException when caller is not OWNER")
    void should_throwAccessDenied_when_deleteWithoutOwnerRole() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId, UUID.randomUUID());
        var member = buildMember(organization, authUserId, OrganizationRoleType.ADMIN);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(member);
        doThrow(new OrganizationAccessDeniedException("Access denied"))
                .when(organizationAccessGuard).requireAtLeast(organizationId, member, OrganizationRoleType.OWNER);

        assertThatThrownBy(() -> organizationService.delete(organizationId))
                .isInstanceOf(OrganizationAccessDeniedException.class);

        verify(organizationMemberRepository, never()).deleteAllByOrganizationId(any());
        verify(organizationRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private Organization buildOrganization(UUID id, UUID ownerId) {
        var organization = new Organization();
        organization.setId(id);
        organization.setName("My Organization");
        organization.setDescription("An organization description");
        organization.setOwnerId(ownerId);
        organization.setDeleted(false);
        return organization;
    }

    private OrganizationMember buildMember(Organization organization, UUID userId, OrganizationRoleType role) {
        var member = new OrganizationMember();
        member.setId(UUID.randomUUID());
        member.setOrganization(organization);
        member.setUserId(userId);
        member.setRole(role);
        member.setDeleted(false);
        return member;
    }

    private OrganizationDto buildOrganizationDto(UUID id, UUID ownerId) {
        return new OrganizationDto(
                id,
                "My Organization",
                "An organization description",
                ownerId
        );
    }
}
