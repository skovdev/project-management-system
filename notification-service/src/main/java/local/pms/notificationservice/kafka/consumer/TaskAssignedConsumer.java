package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.constant.KafkaConstants;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.event.TaskAssignedEvent;

import local.pms.notificationservice.service.NotificationService;

import local.pms.notificationservice.type.NotificationTypeType;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAssignedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaConstants.Topics.TASK_ASSIGNED_TOPIC,
            groupId = KafkaConstants.GroupIds.NOTIFICATION_TASK_ASSIGNED_GROUP_ID)
    public void onTaskAssigned(TaskAssignedEvent event) {
        log.info("Received task-assigned event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.TASK_ASSIGNED_TOPIC, LocalDateTime.now());

        var notification = buildTaskAssignedNotification(event);
        notificationService.save(notification);

        log.info("TASK_ASSIGNED notification created for userId: {}", event.assigneeId());
    }

    private Notification buildTaskAssignedNotification(TaskAssignedEvent event) {
        var notification = new Notification();
        notification.setUserId(event.assigneeId());
        notification.setType(NotificationTypeType.TASK_ASSIGNED);
        notification.setTitle("Task assigned");
        notification.setMessage("You have been assigned to task \"" + event.title() + "\".");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
