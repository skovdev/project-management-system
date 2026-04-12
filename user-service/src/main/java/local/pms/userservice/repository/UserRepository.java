package local.pms.userservice.repository;

import local.pms.userservice.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UUID> {
    Page<User> findAll(Pageable pageable);
    Optional<User> findByAuthUserId(UUID authUserId);
    boolean existsByAuthUserId(UUID authUserId);
    @Query(value = "SELECT COUNT(*) > 0 FROM project_management_system_user WHERE auth_user_id = :authUserId", nativeQuery = true)
    boolean existsByAuthUserIdIncludingDeleted(@Param("authUserId") UUID authUserId);
}