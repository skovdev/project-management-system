package local.pms.organizationservice.listener;

import local.pms.organizationservice.constant.KafkaConstants;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import local.pms.organizationservice.kafka.producer.OrganizationDeletedProducer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationDeletedListenerTest {

    @Mock
    private OrganizationDeletedProducer organizationDeletedProducer;

    @InjectMocks
    private OrganizationDeletedListener listener;

    @Test
    @DisplayName("handleOrganizationDeletedEvent delegates to the producer with the correct topic")
    void should_delegateToProducer_when_eventReceived() {
        var event = new OrganizationDeletedEvent(UUID.randomUUID());

        listener.handleOrganizationDeletedEvent(event);

        verify(organizationDeletedProducer).sendOrganizationDeletedEvent(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);
    }
}
