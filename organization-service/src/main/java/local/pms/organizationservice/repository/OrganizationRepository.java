package local.pms.organizationservice.repository;

import local.pms.organizationservice.entity.Organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Organization o where o.id = :organizationId")
    Optional<Organization> findByIdForUpdate(@Param("organizationId") UUID organizationId);
    @Query("select om.organization from OrganizationMember om where om.userId = :userId")
    Page<Organization> findAllByMemberUserId(@Param("userId") UUID userId, Pageable pageable);
}
