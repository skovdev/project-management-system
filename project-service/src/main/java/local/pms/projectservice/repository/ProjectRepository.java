package local.pms.projectservice.repository;

import local.pms.projectservice.entity.Project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

}