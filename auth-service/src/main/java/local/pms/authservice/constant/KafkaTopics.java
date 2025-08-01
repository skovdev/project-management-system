package local.pms.authservice.constant;

public interface KafkaTopics {
    String USER_DETAILS_CREATED_TOPIC = "user-details-created";
    String USER_DETAILS_FAILED_TOPIC = "user-details-failed";
    String USER_DETAILS_DELETED_TOPIC = "user-details-deleted";
    String USER_DETAILS_DELETED_FAILED_TOPIC = "user-details-deleted-failed";
}