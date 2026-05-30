package local.pms.notificationservice.constant;

public final class KafkaConstants {

    private KafkaConstants() {}

    public static final class GroupIds {
        public static final String NOTIFICATION_USER_DETAILS_CREATION_GROUP_ID = "notification-user-details-creation-group-id";
        public static final String NOTIFICATION_PROJECT_CREATED_GROUP_ID = "notification-project-created-group-id";
        public static final String NOTIFICATION_TASK_CREATED_GROUP_ID = "notification-task-created-group-id";
    }

    public static final class Topics {
        public static final String USER_DETAILS_CREATION_TOPIC = "user-details-creation";
        public static final String PROJECT_CREATED_TOPIC = "project-created";
        public static final String TASK_CREATED_TOPIC = "task-created";
    }
}
