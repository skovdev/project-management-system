package local.pms.notificationservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;

import org.apache.kafka.clients.producer.ProducerConfig;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.HashMap;

/**
 * Configures the Kafka admin client and ensures DLT topics exist for all consumed topics.
 */
@Configuration
public class KafkaAdminConfig {

    @Value("${project-management-system.kafka.server}")
    private String kafkaServer;

    @Value("${project-management-system.kafka.topic.partitions}")
    private int partitions;

    @Value("${project-management-system.kafka.topic.replication-factor}")
    private short replicationFactor;

    /**
     * Provides a {@link KafkaAdmin} bean pointing at the configured broker.
     *
     * @return the Kafka admin client
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        return new KafkaAdmin(props);
    }

    /**
     * Declares Dead-Letter Topics for all topics consumed by the notification-service.
     *
     * @return list of DLT {@link NewTopic} definitions
     */
    @Bean
    public List<NewTopic> dltTopics() {
        return List.of(
                new NewTopic("user-details-creation.DLT", partitions, replicationFactor),
                new NewTopic("project-created.DLT", partitions, replicationFactor),
                new NewTopic("task-created.DLT", partitions, replicationFactor)
        );
    }
}
