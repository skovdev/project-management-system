package local.pms.authservice.kafka.saga.producer.user;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.dto.authuser.UserDetailsDto;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserDetailsCreationProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserDetailsCreationProducer producer;

    @Test
    @DisplayName("sendUserDetailsToCreate sends event to the user-details-creation topic")
    void should_sendEvent_when_sendUserDetailsToCreateCalled() {
        var event = buildEvent();
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToCreate(KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC);
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("sendUserDetailsToCreate sends the correct event payload")
    void should_sendCorrectEvent_when_sendUserDetailsToCreateCalled() {
        var event = buildEvent();
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToCreate(KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, event);
    }

    private UserDetailsCreatedEvent buildEvent() {
        var dto = new UserDetailsDto("Alice", "Smith", "alice@mail.com", UUID.randomUUID());
        return new UserDetailsCreatedEvent(dto);
    }
}
