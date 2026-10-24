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
    @Query("select p.id from Project p where p.organizationId = :organizationId")
    List<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId);
    @Modifying(clearAutomatically = true)
    @Query("update Project p set p.deleted = true where p.organizationId = :organizationId")
    void softDeleteAllByOrganizationId(@Param("organizationId") UUID organizationId);
}