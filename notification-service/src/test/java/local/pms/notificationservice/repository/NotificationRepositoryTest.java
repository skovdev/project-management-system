package local.pms.notificationservice.repository;

import local.pms.notificationservice.entity.Notification;
import local.pms.notificationservice.type.NotificationTypeType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class NotificationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllByUserId returns only notifications for the given userId")
    void should_returnNotificationsForUser_when_findAllByUserId() {
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        persistNotification(userId);
        persistNotification(userId);
        persistNotification(otherUserId);
        entityManager.flush();

        var page = notificationRepository.findAllByUserId(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when notification belongs to another user")
    void should_returnEmpty_when_notificationBelongsToAnotherUser() {
        var ownerId = UUID.randomUUID();
        var requesterId = UUID.randomUUID();
        var notification = persistNotification(ownerId);
        entityManager.flush();

        var result = notificationRepository.findByIdAndUserId(notification.getId(), requesterId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findTop5ByUserIdAndReadFalseOrderByCreatedAtDesc returns at most 5 unread notifications")
    void should_returnAtMostFiveUnread_when_findTop5Unread() {
        var userId = UUID.randomUUID();
        for (int i = 0; i < 7; i++) {
            persistNotification(userId);
        }
        entityManager.flush();

        var result = notificationRepository.findTop5ByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        assertThat(result).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("countByUserIdAndRead returns correct unread count")
    void should_returnCorrectCount_when_countingUnread() {
        var userId = UUID.randomUUID();
        persistNotification(userId);
        persistNotification(userId);
        var readNotification = persistNotification(userId);
        readNotification.setRead(true);
        entityManager.persist(readNotification);
        entityManager.flush();

        long unreadCount = notificationRepository.countByUserIdAndRead(userId, false);

        assertThat(unreadCount).isEqualTo(2);
    }

    private Notification persistNotification(UUID userId) {
        var notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationTypeType.WELCOME);
        notification.setTitle("Welcome to PMS!");
        notification.setMessage("Test message");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return entityManager.persist(notification);
    }
}
