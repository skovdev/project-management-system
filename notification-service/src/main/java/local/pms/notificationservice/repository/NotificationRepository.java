package local.pms.notificationservice.repository;

import local.pms.notificationservice.entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Returns a paginated list of all non-deleted notifications for the given user.
     *
     * @param userId   the owning user's identifier
     * @param pageable pagination and sorting parameters
     * @return page of notifications
     */
    Page<Notification> findAllByUserId(UUID userId, Pageable pageable);

    /**
     * Finds a non-deleted notification by its ID, restricted to the given user.
     *
     * @param id     the notification identifier
     * @param userId the owning user's identifier
     * @return the notification if it exists and belongs to the user
     */
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Returns the five most recent unread notifications for the given user, ordered newest-first.
     * Used to populate the header bell dropdown.
     *
     * @param userId the owning user's identifier
     * @return up to five unread notifications
     */
    List<Notification> findTop5ByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId);

    /**
     * Returns the count of unread notifications for the given user.
     *
     * @param userId the owning user's identifier
     * @param read   read status to filter by
     * @return count of matching notifications
     */
    long countByUserIdAndRead(UUID userId, boolean read);
}
