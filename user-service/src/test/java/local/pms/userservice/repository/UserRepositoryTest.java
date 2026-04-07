package local.pms.userservice.repository;

import local.pms.userservice.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.data.domain.PageRequest;

import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByAuthUserId returns user when authUserId exists")
    void should_returnUser_when_authUserIdExists() {
        var authUserId = UUID.randomUUID();
        persistUser("alice@mail.com", authUserId);

        var result = userRepository.findByAuthUserId(authUserId);

        assertThat(result).isPresent();
        assertThat(result.get().getAuthUserId()).isEqualTo(authUserId);
    }

    @Test
    @DisplayName("findByAuthUserId returns empty when authUserId not found")
    void should_returnEmpty_when_authUserIdNotFound() {
        var result = userRepository.findByAuthUserId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll returns page of active users")
    void should_returnPageOfUsers_when_findAll() {
        persistUser("bob@mail.com", UUID.randomUUID());
        persistUser("carol@mail.com", UUID.randomUUID());
        entityManager.flush();

        var page = userRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("deleteById soft-deletes user so it is no longer found by findById")
    void should_hideUserFromFindById_when_softDeleted() {
        var authUserId = UUID.randomUUID();
        var saved = persistUser("dave@mail.com", authUserId);
        entityManager.flush();

        userRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = userRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes user so it is no longer found by findByAuthUserId")
    void should_hideUserFromFindByAuthUserId_when_softDeleted() {
        var authUserId = UUID.randomUUID();
        var saved = persistUser("eve@mail.com", authUserId);
        entityManager.flush();

        userRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = userRepository.findByAuthUserId(authUserId);
        assertThat(result).isEmpty();
    }

    private User persistUser(String email, UUID authUserId) {
        var user = new User();
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail(email);
        user.setAuthUserId(authUserId);
        user.setDeleted(false);
        return entityManager.persist(user);
    }
}
