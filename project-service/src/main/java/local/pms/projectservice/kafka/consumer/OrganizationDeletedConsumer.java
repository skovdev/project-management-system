package local.pms.projectservice.kafka.consumer;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.OrganizationDeletedEvent;

import local.pms.projectservice.service.ProjectService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationDeletedConsumer {

    private final ProjectService projectService;

    @KafkaListener(topics = KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC,
            groupId = KafkaConstants.GroupIds.ORGANIZATION_DELETED_GROUP_ID)
    public void onOrganizationDeleted(OrganizationDeletedEvent event) {
        log.info("Received organization-deleted event. Topic: {} - Datetime: {}",
                KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, LocalDateTime.now());
        // deleteAllByOrganizationId is idempotent: re-processing is a no-op because soft-deleted
        // projects are excluded by @SQLRestriction, so a second call simply finds zero rows.
        log.info("Deleting all projects for organizationId: {}", event.organizationId());
        projectService.deleteAllByOrganizationId(event.organizationId());
        log.info("All projects deleted successfully for organizationId: {}", event.organizationId());
    }
}
