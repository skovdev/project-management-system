package local.pms.projectservice.repository;

import local.pms.projectservice.entity.Project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findAllByOrganizationId(UUID organizationId, Pageable pageable);
    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<UUID> findIdByOrganizationId(UUID organizationId);
    @Modifying
    @Query("UPDATE Project p SET p.deleted = true WHERE p.organizationId = :organizationId")
    void softDeleteAllByOrganizationId(@Param("organizationId") UUID organizationId);
}
