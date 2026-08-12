package local.pms.organizationservice.service.impl;

import local.pms.organizationservice.dto.AddMemberRequestDto;
import local.pms.organizationservice.dto.OrganizationMemberDto;
import local.pms.organizationservice.dto.UpdateMemberRoleRequestDto;

import local.pms.organizationservice.entity.Organization;
import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.exception.LastOwnerRemovalException;
import local.pms.organizationservice.exception.DuplicateMembershipException;
import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationMemberNotFoundException;

import local.pms.organizationservice.mapping.OrganizationMemberMapping;

import local.pms.organizationservice.repository.OrganizationRepository;
import local.pms.organizationservice.repository.OrganizationMemberRepository;

import local.pms.organizationservice.service.OrganizationMemberService;

import local.pms.organizationservice.type.OrganizationRoleType;

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
public class OrganizationMemberServiceImpl implements OrganizationMemberService {

    private final OrganizationMemberMapping organizationMemberMapping = OrganizationMemberMapping.INSTANCE;

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationAccessGuard organizationAccessGuard;

    @Override
    @Transactional
    public OrganizationMemberDto addMember(UUID organizationId, AddMemberRequestDto request) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();
        var organizationMember = organizationAccessGuard.requireMembership(organizationId, authUserId);
        organizationAccessGuard.requireAtLeast(organizationId, organizationMember, OrganizationRoleType.ADMIN);

        var organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> {
                    log.error("Organization with ID {} not found.", organizationId);
                    return new OrganizationNotFoundException(
                            "Organization with ID " + organizationId + " not found. Please provide a valid organization ID");
                });

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, request.userId())) {
            log.error("User {} is already a member of organization {}.", request.userId(), organizationId);
            throw new DuplicateMembershipException(
                    "User " + request.userId() + " is already a member of organization " + organizationId);
        }

        var member = buildOrganizationMember(request, organization);
        var saved = organizationMemberRepository.save(member);
        log.info("User {} added to organization {} with role {}.", request.userId(), organizationId, request.role());
        return organizationMemberMapping.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationMemberDto> findAll(UUID organizationId, Pageable pageable) {
        organizationAccessGuard.requireMembership(organizationId, organizationAccessGuard.getAuthenticatedUserId());
        return organizationMemberRepository.findAllByOrganizationId(organizationId, pageable)
                .map(organizationMemberMapping::toDto);
    }

    @Override
    @Transactional
    public OrganizationMemberDto updateRole(UUID organizationId, UUID memberId, UpdateMemberRoleRequestDto request) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();
        var callerMember = organizationAccessGuard.requireMembership(organizationId, authUserId);
        organizationAccessGuard.requireAtLeast(organizationId, callerMember, OrganizationRoleType.OWNER);

        var targetMember = findMemberOrThrow(organizationId, memberId);

        if (isLastOwnerDemotion(organizationId, targetMember, request.role())) {
            log.error("Attempted to change the last OWNER's role for member {} in organization {}.", memberId, organizationId);
            throw new LastOwnerRemovalException(
                    "Cannot change role: organization " + organizationId + " must have at least one OWNER");
        }

        targetMember.setRole(request.role());
        var saved = organizationMemberRepository.save(targetMember);
        log.info("Member {} role updated to {} in organization {}.", memberId, request.role(), organizationId);
        return organizationMemberMapping.toDto(saved);
    }

    @Override
    @Transactional
    public void removeMember(UUID organizationId, UUID memberId) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();
        var callerMember = organizationAccessGuard.requireMembership(organizationId, authUserId);

        var targetMember = findMemberOrThrow(organizationId, memberId);

        boolean isSelfRemoval = targetMember.getUserId().equals(authUserId);
        if (!isSelfRemoval) {
            organizationAccessGuard.requireAtLeast(organizationId, callerMember, OrganizationRoleType.ADMIN);
        }

        if (targetMember.getRole() == OrganizationRoleType.OWNER
                && organizationMemberRepository.countByOrganizationIdAndRole(organizationId, OrganizationRoleType.OWNER) <= 1) {
            log.error("Attempted to remove the last OWNER (member {}) from organization {}.", memberId, organizationId);
            throw new LastOwnerRemovalException(
                    "Cannot remove member: organization " + organizationId + " must have at least one OWNER");
        }

        organizationMemberRepository.deleteById(memberId);
        log.info("Member {} removed from organization {}.", memberId, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationMemberDto getMyMembership(UUID organizationId) {
        var member = organizationAccessGuard.requireMembership(organizationId, organizationAccessGuard.getAuthenticatedUserId());
        return organizationMemberMapping.toDto(member);
    }

    private OrganizationMember buildOrganizationMember(AddMemberRequestDto request, Organization organization) {
        var member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUserId(request.userId());
        member.setRole(request.role());
        return member;
    }

    private OrganizationMember findMemberOrThrow(UUID organizationId, UUID memberId) {
        return organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)
                .orElseThrow(() -> {
                    log.error("Member with ID {} not found in organization {}.", memberId, organizationId);
                    return new OrganizationMemberNotFoundException(
                            "Member with ID " + memberId + " not found in organization " + organizationId);
                });
    }

    private boolean isLastOwnerDemotion(UUID organizationId, OrganizationMember targetMember, OrganizationRoleType newRole) {
        return targetMember.getRole() == OrganizationRoleType.OWNER
                && newRole != OrganizationRoleType.OWNER
                && organizationMemberRepository.countByOrganizationIdAndRole(organizationId, OrganizationRoleType.OWNER) <= 1;
    }
}
