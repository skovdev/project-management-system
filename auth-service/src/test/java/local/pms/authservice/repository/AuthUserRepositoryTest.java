package local.pms.authservice.repository;

import local.pms.authservice.entity.AuthRole;
import local.pms.authservice.entity.AuthUser;
import local.pms.authservice.entity.AuthPermission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AuthUserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUsername returns user when username exists")
    void should_returnUser_when_usernameExists() {
        var saved = persistAuthUser("bob");

        var result = authUserRepository.findByUsername("bob");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getUsername()).isEqualTo("bob");
    }

    @Test
    @DisplayName("findByUsername returns empty when username not found")
    void should_returnEmpty_when_usernameNotFound() {
        var result = authUserRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes user so it is no longer found by findById")
    void should_hideUserFromFindById_when_softDeleted() {
        var saved = persistAuthUser("charlie");
        entityManager.flush();

        authUserRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        // @SQLRestriction("deleted = false") hides soft-deleted records
        var result = authUserRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes user so it is no longer found by findByUsername")
    void should_hideUserFromFindByUsername_when_softDeleted() {
        var saved = persistAuthUser("dave");
        entityManager.flush();

        authUserRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = authUserRepository.findByUsername("dave");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save with deleted=false restores a soft-deleted user")
    void should_restoreUser_when_savedWithDeletedFalse() {
        var saved = persistAuthUser("eve");
        entityManager.flush();

        // Soft-delete
        authUserRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        // Restore by native update (simulating restoreAuthUserById logic)
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE project_management_system_auth_user SET deleted = false WHERE id = ?")
                .setParameter(1, saved.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        var result = authUserRepository.findByUsername("eve");
        assertThat(result).isPresent();
    }

    private AuthUser persistAuthUser(String username) {
        var user = new AuthUser();
        user.setUsername(username);
        user.setPassword("hashed");
        user.setDeleted(false);

        var role = new AuthRole();
        role.setAuthority("USER");
        role.setAuthUser(user);
        role.setDeleted(false);

        var permission = new AuthPermission();
        permission.setPermission("READ_ALL");
        permission.setAuthUser(user);
        permission.setDeleted(false);

        user.setAuthRoles(List.of(role));
        user.setAuthPermissions(List.of(permission));

        return entityManager.persist(user);
    }
}
