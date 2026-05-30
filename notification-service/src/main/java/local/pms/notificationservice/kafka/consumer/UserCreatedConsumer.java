package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.constant.KafkaConstants;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.event.UserDetailsCreatedEvent;

import local.pms.notificationservice.service.NotificationService;

import local.pms.notificationservice.type.NotificationTypeType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Kafka consumer that listens to the {@code user-details-creation} topic and creates
 * a WELCOME notification for every newly registered user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedConsumer {

    private final NotificationService notificationService;

    /**
     * Processes a {@link UserDetailsCreatedEvent} and persists a WELCOME notification.
     * Idempotency is not enforced at the consumer level; duplicate messages produce duplicate
     * notifications which are harmless — the user simply sees more than one welcome entry
     * and can dismiss them.
     *
     * @param event the user-created event payload
     */
    @KafkaListener(
            topics = KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC,
            groupId = KafkaConstants.GroupIds.NOTIFICATION_USER_DETAILS_CREATION_GROUP_ID)
    public void onUserCreated(UserDetailsCreatedEvent event) {
        log.info("Received user-details-creation event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, LocalDateTime.now());

        var notification = buildWelcomeNotification(event);
        notificationService.save(notification);

        log.info("WELCOME notification created for authUserId: {}", event.userDetailsDto().authUserId());
    }

    private Notification buildWelcomeNotification(UserDetailsCreatedEvent event) {
        var userDto = event.userDetailsDto();
        var notification = new Notification();
        notification.setUserId(userDto.authUserId());
        notification.setType(NotificationTypeType.WELCOME);
        notification.setTitle("Welcome to PMS!");
        notification.setMessage("Hi " + userDto.firstName() + ", your account has been created successfully. Start by creating your first project.");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
