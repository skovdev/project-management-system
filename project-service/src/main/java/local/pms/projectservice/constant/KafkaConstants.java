package local.pms.projectservice.constant;

/**
 * Kafka topic and group ID constants for the project-service.
 */
public final class KafkaConstants {

    private KafkaConstants() {}

    public static final class GroupIds {
        public static final String PROJECT_DELETED_GROUP_ID = "project-deleted-group-id";
        public static final String ORGANIZATION_DELETED_GROUP_ID = "organization-deleted-group-id";
        public static final String PROJECT_DLT_MONITOR_GROUP_ID = "project-dlt-monitor-group-id";
    }

    public static final class Topics {
        public static final String PROJECT_CREATED_TOPIC = "project-created";
        public static final String PROJECT_DELETED_TOPIC = "project-deleted";
        public static final String ORGANIZATION_DELETED_TOPIC = "organization-deleted";
        public static final String ORGANIZATION_DELETED_DLT_TOPIC = "organization-deleted.DLT";
    }
}
