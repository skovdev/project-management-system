package local.pms.organizationservice.service.impl;

import local.pms.organizationservice.dto.OrganizationDto;

import local.pms.organizationservice.entity.Organization;
import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import local.pms.organizationservice.exception.OrganizationNotFoundException;

import local.pms.organizationservice.mapping.OrganizationMapping;

import local.pms.organizationservice.repository.OrganizationRepository;
import local.pms.organizationservice.repository.OrganizationMemberRepository;

import local.pms.organizationservice.service.OrganizationService;

import local.pms.organizationservice.type.OrganizationRoleType;

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
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationMapping organizationMapping = OrganizationMapping.INSTANCE;

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationAccessGuard organizationAccessGuard;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrganizationDto create(OrganizationDto organizationDto) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();

        var organization = organizationMapping.toEntity(organizationDto);
        organization.setOwnerId(authUserId);
        var savedOrganization = organizationRepository.save(organization);

        var ownerMember = new OrganizationMember();
        ownerMember.setOrganization(savedOrganization);
        ownerMember.setUserId(authUserId);
        ownerMember.setRole(OrganizationRoleType.OWNER);
        organizationMemberRepository.save(ownerMember);

        log.info("Organization created with ID: {}, owner: {}", savedOrganization.getId(), authUserId);
        return organizationMapping.toDto(savedOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAll(Pageable pageable) {
        return organizationRepository.findAllByMemberUserId(organizationAccessGuard.getAuthenticatedUserId(), pageable)
                .map(organizationMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDto findById(UUID organizationId) {
        organizationAccessGuard.requireMembership(organizationId, organizationAccessGuard.getAuthenticatedUserId());
        return organizationMapping.toDto(findOrganizationOrThrow(organizationId));
    }

    @Override
    @Transactional
    public OrganizationDto update(UUID organizationId, OrganizationDto organizationDto) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();
        var member = organizationAccessGuard.requireMembership(organizationId, authUserId);
        organizationAccessGuard.requireAtLeast(organizationId, member, OrganizationRoleType.ADMIN);

        var organization = findOrganizationOrThrow(organizationId);
        organization.setName(organizationDto.name());
        organization.setDescription(organizationDto.description());
        var updated = organizationRepository.save(organization);
        log.info("Organization with ID {} updated successfully.", organizationId);
        return organizationMapping.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID organizationId) {
        UUID authUserId = organizationAccessGuard.getAuthenticatedUserId();
        var member = organizationAccessGuard.requireMembership(organizationId, authUserId);
        organizationAccessGuard.requireAtLeast(organizationId, member, OrganizationRoleType.OWNER);

        findOrganizationOrThrow(organizationId);
        // Cascade the member rows synchronously, in the same transaction, so the membership
        // check other services rely on (which only looks at OrganizationMember, not Organization
        // itself) fails immediately rather than leaving deleted orgs fully accessible.
        organizationMemberRepository.deleteAllByOrganizationId(organizationId);
        organizationRepository.deleteById(organizationId);
        log.info("Organization with ID {} deleted successfully.", organizationId);
        eventPublisher.publishEvent(new OrganizationDeletedEvent(organizationId));
    }

    private Organization findOrganizationOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> {
                    log.error("Organization with ID {} not found.", organizationId);
                    return new OrganizationNotFoundException(
                            "Organization with ID " + organizationId + " not found. Please provide a valid organization ID");
                });
    }
}
