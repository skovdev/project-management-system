package local.pms.organizationservice.repository;

import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.type.OrganizationRoleType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {
    Page<OrganizationMember> findAllByOrganizationId(UUID organizationId, Pageable pageable);
    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    Optional<OrganizationMember> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    long countByOrganizationIdAndRole(UUID organizationId, OrganizationRoleType role);
    void deleteAllByOrganizationId(UUID organizationId);
}
