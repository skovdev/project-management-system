package local.pms.authservice.constant;

public final class KafkaConstants {

    private KafkaConstants() {}

    public static final class GroupIds {
        public static final String AUTH_USER_DETAILS_CREATION_GROUP_ID = "auth-user-details-creation-group-id";
        public static final String AUTH_USER_DETAILS_DELETION_GROUP_ID = "auth-user-details-deletion-group-id";
    }

    public static final class Topics {
        public static final String USER_DETAILS_CREATION_TOPIC = "user-details-creation";
        public static final String USER_DETAILS_CREATION_FAILED_TOPIC = "user-details-creation-failed";
        public static final String USER_DETAILS_DELETION_TOPIC = "user-details-deletion";
        public static final String USER_DETAILS_DELETION_FAILED_TOPIC = "user-details-deletion-failed";
    }
}

