package local.pms.organizationservice.service;

import local.pms.organizationservice.dto.OrganizationDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for organization management operations.
 */
public interface OrganizationService {

    /**
     * Creates a new organization; the caller becomes its first OWNER member.
     *
     * @param organizationDto the organization data to create
     * @return the created organization DTO
     */
    OrganizationDto create(OrganizationDto organizationDto);

    /**
     * Retrieves a paginated list of organizations the caller belongs to.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of organization DTOs
     */
    Page<OrganizationDto> findAll(Pageable pageable);

    /**
     * Finds an organization by its identifier; the caller must be a member.
     *
     * @param organizationId the unique organization identifier
     * @return the organization DTO
     */
    OrganizationDto findById(UUID organizationId);

    /**
     * Updates an existing organization; the caller must be an OWNER or ADMIN member.
     *
     * @param organizationId  the unique organization identifier
     * @param organizationDto the updated organization data
     * @return the updated organization DTO
     */
    OrganizationDto update(UUID organizationId, OrganizationDto organizationDto);

    /**
     * Soft-deletes an organization; the caller must be an OWNER member.
     *
     * @param organizationId the unique organization identifier
     */
    void delete(UUID organizationId);
}
