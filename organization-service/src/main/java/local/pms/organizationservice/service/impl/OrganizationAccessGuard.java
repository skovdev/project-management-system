package local.pms.organizationservice.service.impl;

import local.pms.organizationservice.config.jwt.JwtTokenProvider;

import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;

import local.pms.organizationservice.repository.OrganizationMemberRepository;

import local.pms.organizationservice.service.TokenService;

import local.pms.organizationservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Shared authentication/authorization checks used by both
 * {@link OrganizationServiceImpl} and {@link OrganizationMemberServiceImpl},
 * since both need to resolve the caller's identity and organization membership role.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationAccessGuard {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public UUID getAuthenticatedUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract auth user ID.");
            throw new OrganizationAccessDeniedException("Authentication token is missing or invalid. Please provide a valid token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }

    public OrganizationMember requireMembership(UUID organizationId, UUID authUserId) {
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, authUserId)
                .orElseThrow(() -> {
                    log.error("Organization with ID {} not found or access denied for user {}.", organizationId, authUserId);
                    return new OrganizationNotFoundException(
                            "Organization with ID " + organizationId + " not found. Please provide a valid organization ID");
                });
    }

    public void requireRole(UUID organizationId, OrganizationMember member, OrganizationRoleType... allowedRoles) {
        for (OrganizationRoleType allowed : allowedRoles) {
            if (member.getRole() == allowed) {
                return;
            }
        }
        log.error("User {} with role {} attempted a restricted action on organization {}.",
                member.getUserId(), member.getRole(), organizationId);
        throw new OrganizationAccessDeniedException(
                "Access denied: you do not have sufficient permissions in organization with ID " + organizationId);
    }
}
