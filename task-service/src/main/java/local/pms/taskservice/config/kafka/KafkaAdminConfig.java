package local.pms.taskservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;

import org.apache.kafka.clients.producer.ProducerConfig;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Configuration
public class KafkaAdminConfig {

    @Value("${project-management-system.kafka.server}")
    private String kafkaServer;

    @Value("${project-management-system.kafka.topic.partitions}")
    private int partitions;

    @Value("${project-management-system.kafka.topic.replication-factor}")
    private short replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        return new KafkaAdmin(props);
    }

    @Bean
    public List<NewTopic> dltTopics() {
        return List.of(
                new NewTopic("project-deleted.DLT", partitions, replicationFactor)
        );
    }
}
