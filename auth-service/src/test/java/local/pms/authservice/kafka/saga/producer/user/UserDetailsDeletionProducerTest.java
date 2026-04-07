package local.pms.authservice.kafka.saga.producer.user;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.event.UserDetailsDeletedEvent;

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
class UserDetailsDeletionProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserDetailsDeletionProducer producer;

    @Test
    @DisplayName("sendUserDetailsToDelete sends authUserId to the user-details-deletion topic")
    void should_sendAuthUserId_when_sendUserDetailsToDeleteCalled() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToDelete(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC);
        assertThat(payloadCaptor.getValue()).isEqualTo(authUserId);
    }

    @Test
    @DisplayName("sendUserDetailsToDelete sends the correct authUserId payload")
    void should_sendCorrectAuthUserId_when_sendUserDetailsToDeleteCalled() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC), eq(authUserId)))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToDelete(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, authUserId);
    }
}
