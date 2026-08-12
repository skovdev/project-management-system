package local.pms.organizationservice.service;

import local.pms.organizationservice.dto.AddMemberRequestDto;
import local.pms.organizationservice.dto.UpdateMemberRoleRequestDto;

import local.pms.organizationservice.entity.Organization;
import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.exception.LastOwnerRemovalException;
import local.pms.organizationservice.exception.DuplicateMembershipException;
import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;
import local.pms.organizationservice.exception.OrganizationMemberNotFoundException;

import local.pms.organizationservice.repository.OrganizationRepository;
import local.pms.organizationservice.repository.OrganizationMemberRepository;

import local.pms.organizationservice.service.impl.OrganizationAccessGuard;
import local.pms.organizationservice.service.impl.OrganizationMemberServiceImpl;

import local.pms.organizationservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

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
class OrganizationMemberServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private OrganizationAccessGuard organizationAccessGuard;

    @InjectMocks
    private OrganizationMemberServiceImpl organizationMemberService;

    @Test
    @DisplayName("getMyMembership returns the caller's own membership")
    void should_returnCallerMembership_when_getMyMembership() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.ADMIN);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);

        var result = organizationMemberService.getMyMembership(organizationId);

        assertThat(result.userId()).isEqualTo(authUserId);
        assertThat(result.role()).isEqualTo(OrganizationRoleType.ADMIN);
    }

    @Test
    @DisplayName("getMyMembership propagates OrganizationNotFoundException when caller is not a member")
    void should_propagateNotFound_when_getMyMembershipNotAMember() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + organizationId + " not found"));

        assertThatThrownBy(() -> organizationMemberService.getMyMembership(organizationId))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    @DisplayName("addMember saves new membership when caller is OWNER or ADMIN")
    void should_saveMember_when_callerIsOwnerOrAdmin() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var newUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var request = new AddMemberRequestDto(newUserId, OrganizationRoleType.MEMBER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, newUserId)).thenReturn(false);
        when(organizationMemberRepository.save(any(OrganizationMember.class)))
                .thenReturn(buildMember(organization, newUserId, OrganizationRoleType.MEMBER));

        var result = organizationMemberService.addMember(organizationId, request);

        assertThat(result.userId()).isEqualTo(newUserId);
        assertThat(result.role()).isEqualTo(OrganizationRoleType.MEMBER);
    }

    @Test
    @DisplayName("addMember throws OrganizationAccessDeniedException when caller lacks permission")
    void should_throwAccessDenied_when_addMemberWithoutPermission() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.MEMBER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        doThrow(new OrganizationAccessDeniedException("Access denied"))
                .when(organizationAccessGuard).requireAtLeast(organizationId, callerMember, OrganizationRoleType.ADMIN);

        assertThatThrownBy(() -> organizationMemberService.addMember(organizationId, new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER)))
                .isInstanceOf(OrganizationAccessDeniedException.class);

        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("addMember throws OrganizationNotFoundException when organization missing")
    void should_throwNotFound_when_addMemberOrganizationMissing() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationMemberService.addMember(organizationId, new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER)))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    @DisplayName("addMember throws DuplicateMembershipException when user is already a member")
    void should_throwDuplicate_when_addMemberAlreadyMember() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var newUserId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, newUserId)).thenReturn(true);

        assertThatThrownBy(() -> organizationMemberService.addMember(organizationId, new AddMemberRequestDto(newUserId, OrganizationRoleType.MEMBER)))
                .isInstanceOf(DuplicateMembershipException.class);

        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll returns page of members when caller is a member")
    void should_returnPageOfMembers_when_findAll() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.MEMBER);
        var page = new PageImpl<>(List.of(callerMember), pageable, 1);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findAllByOrganizationId(organizationId, pageable)).thenReturn(page);

        var result = organizationMemberService.findAll(organizationId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateRole updates role when caller is OWNER")
    void should_updateRole_when_callerIsOwner() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var targetMember = buildMember(organization, UUID.randomUUID(), OrganizationRoleType.MEMBER);
        targetMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(targetMember));
        when(organizationMemberRepository.save(targetMember)).thenReturn(targetMember);

        var result = organizationMemberService.updateRole(organizationId, memberId, new UpdateMemberRoleRequestDto(OrganizationRoleType.ADMIN));

        assertThat(result.role()).isEqualTo(OrganizationRoleType.ADMIN);
    }

    @Test
    @DisplayName("updateRole throws OrganizationAccessDeniedException when caller is not OWNER")
    void should_throwAccessDenied_when_updateRoleWithoutOwnerRole() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.ADMIN);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        doThrow(new OrganizationAccessDeniedException("Access denied"))
                .when(organizationAccessGuard).requireAtLeast(organizationId, callerMember, OrganizationRoleType.OWNER);

        assertThatThrownBy(() -> organizationMemberService.updateRole(organizationId, memberId, new UpdateMemberRoleRequestDto(OrganizationRoleType.MEMBER)))
                .isInstanceOf(OrganizationAccessDeniedException.class);

        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole throws OrganizationMemberNotFoundException when target member missing")
    void should_throwNotFound_when_updateRoleMemberMissing() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationMemberService.updateRole(organizationId, memberId, new UpdateMemberRoleRequestDto(OrganizationRoleType.MEMBER)))
                .isInstanceOf(OrganizationMemberNotFoundException.class);
    }

    @Test
    @DisplayName("updateRole throws LastOwnerRemovalException when demoting the last OWNER")
    void should_throwLastOwnerRemoval_when_demotingLastOwner() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var targetMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        targetMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(targetMember));
        when(organizationMemberRepository.countByOrganizationIdAndRole(organizationId, OrganizationRoleType.OWNER)).thenReturn(1L);

        assertThatThrownBy(() -> organizationMemberService.updateRole(organizationId, memberId, new UpdateMemberRoleRequestDto(OrganizationRoleType.MEMBER)))
                .isInstanceOf(LastOwnerRemovalException.class);

        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeMember removes member when caller is OWNER or ADMIN")
    void should_removeMember_when_callerIsOwnerOrAdmin() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var targetMember = buildMember(organization, UUID.randomUUID(), OrganizationRoleType.MEMBER);
        targetMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(targetMember));

        organizationMemberService.removeMember(organizationId, memberId);

        verify(organizationMemberRepository).deleteById(memberId);
    }

    @Test
    @DisplayName("removeMember allows self-removal without OWNER/ADMIN role")
    void should_allowSelfRemoval_when_targetIsCaller() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.MEMBER);
        callerMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(callerMember));

        organizationMemberService.removeMember(organizationId, memberId);

        verify(organizationMemberRepository).deleteById(memberId);
        verify(organizationAccessGuard, never()).requireAtLeast(any(), any(), any());
    }

    @Test
    @DisplayName("removeMember throws OrganizationAccessDeniedException when caller lacks permission for another member")
    void should_throwAccessDenied_when_removeMemberWithoutPermission() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.MEMBER);
        var targetMember = buildMember(organization, UUID.randomUUID(), OrganizationRoleType.MEMBER);
        targetMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(targetMember));
        doThrow(new OrganizationAccessDeniedException("Access denied"))
                .when(organizationAccessGuard).requireAtLeast(organizationId, callerMember, OrganizationRoleType.ADMIN);

        assertThatThrownBy(() -> organizationMemberService.removeMember(organizationId, memberId))
                .isInstanceOf(OrganizationAccessDeniedException.class);

        verify(organizationMemberRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("removeMember throws OrganizationMemberNotFoundException when target member missing")
    void should_throwNotFound_when_removeMemberMissing() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationMemberService.removeMember(organizationId, memberId))
                .isInstanceOf(OrganizationMemberNotFoundException.class);
    }

    @Test
    @DisplayName("removeMember throws LastOwnerRemovalException when removing the last OWNER")
    void should_throwLastOwnerRemoval_when_removingLastOwner() {
        var organizationId = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var organization = buildOrganization(organizationId);
        var callerMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        var targetMember = buildMember(organization, authUserId, OrganizationRoleType.OWNER);
        targetMember.setId(memberId);

        when(organizationAccessGuard.getAuthenticatedUserId()).thenReturn(authUserId);
        when(organizationAccessGuard.requireMembership(organizationId, authUserId)).thenReturn(callerMember);
        when(organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)).thenReturn(Optional.of(targetMember));
        when(organizationMemberRepository.countByOrganizationIdAndRole(organizationId, OrganizationRoleType.OWNER)).thenReturn(1L);

        assertThatThrownBy(() -> organizationMemberService.removeMember(organizationId, memberId))
                .isInstanceOf(LastOwnerRemovalException.class);

        verify(organizationMemberRepository, never()).deleteById(any());
    }

    private Organization buildOrganization(UUID id) {
        var organization = new Organization();
        organization.setId(id);
        organization.setName("My Organization");
        organization.setDescription("An organization description");
        organization.setOwnerId(UUID.randomUUID());
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
}
