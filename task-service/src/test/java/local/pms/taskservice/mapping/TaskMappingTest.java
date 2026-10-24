package local.pms.taskservice.mapping;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMappingTest {

    @Test
    @DisplayName("toEntity ignores client-supplied id, userId, organizationId and assigneeId")
    void should_ignoreServerOwnedFields_when_mappingToEntity() {
        var dto = new TaskDto(UUID.randomUUID().toString(), "Title", "Description",
                TaskStatusType.values()[0], TaskPriorityType.values()[0], true,
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                null, null, UUID.randomUUID().toString());

        var entity = TaskMapping.INSTANCE.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isNull();
        assertThat(entity.getOrganizationId()).isNull();
        assertThat(entity.getAssigneeId()).isNull();
        assertThat(entity.getTitle()).isEqualTo("Title");
    }
}
