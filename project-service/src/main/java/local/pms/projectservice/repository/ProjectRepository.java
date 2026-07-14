package local.pms.projectservice.repository;

import local.pms.projectservice.entity.Project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findAllByOrganizationId(UUID organizationId, Pageable pageable);
    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);
}