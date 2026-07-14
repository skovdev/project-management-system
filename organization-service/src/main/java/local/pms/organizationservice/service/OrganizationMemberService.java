package local.pms.organizationservice.service;

import local.pms.organizationservice.dto.AddMemberRequestDto;
import local.pms.organizationservice.dto.OrganizationMemberDto;
import local.pms.organizationservice.dto.UpdateMemberRoleRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for organization membership management operations.
 */
public interface OrganizationMemberService {

    /**
     * Adds a new member to an organization; the caller must be an OWNER or ADMIN member.
     *
     * @param organizationId the unique organization identifier
     * @param request        the user to add and their role
     * @return the created membership DTO
     */
    OrganizationMemberDto addMember(UUID organizationId, AddMemberRequestDto request);

    /**
     * Retrieves a paginated list of an organization's members; the caller must be a member.
     *
     * @param organizationId the unique organization identifier
     * @param pageable       pagination and sorting parameters
     * @return a page of membership DTOs
     */
    Page<OrganizationMemberDto> findAll(UUID organizationId, Pageable pageable);

    /**
     * Changes a member's role; the caller must be an OWNER member.
     *
     * @param organizationId the unique organization identifier
     * @param memberId       the membership identifier to update
     * @param request        the new role
     * @return the updated membership DTO
     */
    OrganizationMemberDto updateRole(UUID organizationId, UUID memberId, UpdateMemberRoleRequestDto request);

    /**
     * Removes a member from an organization (or lets a member remove themselves).
     *
     * @param organizationId the unique organization identifier
     * @param memberId       the membership identifier to remove
     */
    void removeMember(UUID organizationId, UUID memberId);

    /**
     * Retrieves the caller's own membership in an organization; used by other
     * services (via Feign, forwarding the caller's JWT) to verify org access.
     *
     * @param organizationId the unique organization identifier
     * @return the caller's membership DTO
     */
    OrganizationMemberDto getMyMembership(UUID organizationId);
}
