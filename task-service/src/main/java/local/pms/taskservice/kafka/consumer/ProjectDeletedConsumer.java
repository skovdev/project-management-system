package local.pms.taskservice.kafka.consumer;

import local.pms.taskservice.constant.KafkaConstants;

import local.pms.taskservice.event.ProjectDeletedEvent;

import local.pms.taskservice.service.TaskService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectDeletedConsumer {

    private final TaskService taskService;

    @KafkaListener(topics = KafkaConstants.Topics.PROJECT_DELETED_TOPIC,
            groupId = KafkaConstants.GroupIds.PROJECT_DELETED_GROUP_ID)
    public void onProjectDeleted(ProjectDeletedEvent event) {
        log.info("Received project-deleted event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.PROJECT_DELETED_TOPIC, LocalDateTime.now());
        try {
            log.info("Deleting all tasks for projectId: {}", event.projectId());
            taskService.deleteAllByProjectId(event.projectId());
            log.info("All tasks deleted successfully for projectId: {}", event.projectId());
        } catch (Exception e) {
            log.error("Failed to delete tasks for projectId: {}. Error: {}", event.projectId(), e.getMessage());
        }
    }
}
