package local.pms.authservice.repository;

import local.pms.authservice.entity.AuthRole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthRoleRepository extends JpaRepository<AuthRole, UUID> {}