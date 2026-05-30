package local.pms.notificationservice.service;

import local.pms.notificationservice.dto.NotificationDto;
import local.pms.notificationservice.entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Business operations for managing user notifications.
 */
public interface NotificationService {

    /**
     * Returns a paginated list of all notifications for the authenticated user.
     *
     * @param pageable pagination and sorting parameters
     * @return page of notification DTOs
     */
    Page<NotificationDto> findAll(Pageable pageable);

    /**
     * Returns the five most recent unread notifications for the authenticated user.
     * Intended for the header bell dropdown.
     *
     * @return list of up to five unread notifications
     */
    List<NotificationDto> findUnread();

    /**
     * Returns a single notification by ID, restricted to the authenticated user.
     *
     * @param notificationId the notification identifier
     * @return the notification DTO
     */
    NotificationDto findById(UUID notificationId);

    /**
     * Marks the given notification as read for the authenticated user.
     *
     * @param notificationId the notification identifier
     * @return the updated notification DTO
     */
    NotificationDto markAsRead(UUID notificationId);

    /**
     * Soft-deletes the given notification for the authenticated user.
     *
     * @param notificationId the notification identifier
     */
    void delete(UUID notificationId);

    /**
     * Persists a new notification entity. Called internally by Kafka consumers.
     *
     * @param notification the entity to save
     */
    void save(Notification notification);
}
