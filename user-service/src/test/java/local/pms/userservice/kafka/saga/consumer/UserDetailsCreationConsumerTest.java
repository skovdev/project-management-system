package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaConstants;

import local.pms.userservice.dto.UserDetailsDto;

import local.pms.userservice.event.UserDetailsCreatedEvent;

import local.pms.userservice.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class UserDetailsCreationConsumerTest {

    @Mock
    private UserService userService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserDetailsCreationConsumer consumer;

    @Test
    @DisplayName("receiveUserDataToCreate calls userService.save with mapped UserDto")
    void should_callSave_when_receiveUserDataToCreate() {
        var authUserId = UUID.randomUUID();
        var event = buildEvent(authUserId);
        doNothing().when(userService).save(any());

        consumer.receiveUserDataToCreate(event);

        var captor = ArgumentCaptor.forClass(local.pms.userservice.dto.UserDto.class);
        verify(userService).save(captor.capture());
        assertThat(captor.getValue().firstName()).isEqualTo("Alice");
        assertThat(captor.getValue().email()).isEqualTo("alice@mail.com");
        assertThat(captor.getValue().authUserId()).isEqualTo(authUserId);
    }

    @Test
    @DisplayName("receiveUserDataToCreate publishes compensation event when save fails")
    void should_publishCompensationEvent_when_saveFails() {
        var authUserId = UUID.randomUUID();
        var event = buildEvent(authUserId);
        doThrow(new RuntimeException("DB error")).when(userService).save(any());

        consumer.receiveUserDataToCreate(event);

        verify(kafkaTemplate).send(
                eq(KafkaConstants.Topics.USER_DETAILS_CREATION_FAILED_TOPIC),
                eq(event));
    }

    @Test
    @DisplayName("receiveUserDataToCreate does not publish compensation when save succeeds")
    void should_notPublishCompensationEvent_when_saveSucceeds() {
        var event = buildEvent(UUID.randomUUID());
        doNothing().when(userService).save(any());

        consumer.receiveUserDataToCreate(event);

        verify(kafkaTemplate, never()).send(any(), any());
    }

    private UserDetailsCreatedEvent buildEvent(UUID authUserId) {
        var dto = new UserDetailsDto("Alice", "Smith", "alice@mail.com", authUserId);
        return new UserDetailsCreatedEvent(dto);
    }
}
