package local.pms.authservice.repository;

import local.pms.authservice.entity.AuthUser;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByUsername(String username);

    @Query(value = "SELECT * FROM project_management_system_auth_user WHERE id = :id", nativeQuery = true)
    Optional<AuthUser> findByIdIncludingDeleted(@Param("id") UUID id);
}
