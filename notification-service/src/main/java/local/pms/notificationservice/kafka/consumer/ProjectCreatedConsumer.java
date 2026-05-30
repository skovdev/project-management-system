package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.constant.KafkaConstants;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.event.ProjectCreatedEvent;

import local.pms.notificationservice.service.NotificationService;

import local.pms.notificationservice.type.NotificationTypeType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Kafka consumer that listens to the {@code project-created} topic and creates
 * a PROJECT_CREATED notification for the project owner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCreatedConsumer {

    private final NotificationService notificationService;

    /**
     * Processes a {@link ProjectCreatedEvent} and persists a PROJECT_CREATED notification.
     *
     * @param event the project-created event payload
     */
    @KafkaListener(
            topics = KafkaConstants.Topics.PROJECT_CREATED_TOPIC,
            groupId = KafkaConstants.GroupIds.NOTIFICATION_PROJECT_CREATED_GROUP_ID)
    public void onProjectCreated(ProjectCreatedEvent event) {
        log.info("Received project-created event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.PROJECT_CREATED_TOPIC, LocalDateTime.now());

        var notification = buildProjectCreatedNotification(event);
        notificationService.save(notification);

        log.info("PROJECT_CREATED notification created for userId: {}", event.userId());
    }

    private Notification buildProjectCreatedNotification(ProjectCreatedEvent event) {
        var notification = new Notification();
        notification.setUserId(event.userId());
        notification.setType(NotificationTypeType.PROJECT_CREATED);
        notification.setTitle("Project created");
        notification.setMessage("Your project \"" + event.title() + "\" has been created successfully.");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
