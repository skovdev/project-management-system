package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.constant.KafkaConstants;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.event.TaskCreatedEvent;

import local.pms.notificationservice.service.NotificationService;

import local.pms.notificationservice.type.NotificationTypeType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Kafka consumer that listens to the {@code task-created} topic and creates
 * a TASK_CREATED notification for the task owner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreatedConsumer {

    private final NotificationService notificationService;

    /**
     * Processes a {@link TaskCreatedEvent} and persists a TASK_CREATED notification.
     *
     * @param event the task-created event payload
     */
    @KafkaListener(
            topics = KafkaConstants.Topics.TASK_CREATED_TOPIC,
            groupId = KafkaConstants.GroupIds.NOTIFICATION_TASK_CREATED_GROUP_ID)
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Received task-created event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.TASK_CREATED_TOPIC, LocalDateTime.now());

        var notification = buildTaskCreatedNotification(event);
        notificationService.save(notification);

        log.info("TASK_CREATED notification created for userId: {}", event.userId());
    }

    private Notification buildTaskCreatedNotification(TaskCreatedEvent event) {
        var notification = new Notification();
        notification.setUserId(event.userId());
        notification.setType(NotificationTypeType.TASK_CREATED);
        notification.setTitle("Task created");
        notification.setMessage("Your task \"" + event.title() + "\" has been created successfully.");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
