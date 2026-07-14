package local.pms.organizationservice.constant;

/**
 * Kafka topic constants for the organization-service.
 */
public final class KafkaConstants {

    private KafkaConstants() {}

    public static final class Topics {
        private Topics() {}
        public static final String ORGANIZATION_DELETED_TOPIC = "organization-deleted";
    }
}
