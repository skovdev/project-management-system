package local.pms.taskservice.constant;

/**
 * Kafka topic and group ID constants for the task-service.
 */
public final class KafkaConstants {

    private KafkaConstants() {}

    public static final class GroupIds {
        public static final String PROJECT_DELETED_GROUP_ID = "project-deleted-group-id";
    }

    public static final class Topics {
        public static final String PROJECT_DELETED_TOPIC = "project-deleted";
    }
}
