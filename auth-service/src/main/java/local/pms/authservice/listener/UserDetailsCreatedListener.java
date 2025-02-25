package local.pms.authservice.listener;

import local.pms.authservice.constant.KafkaTopics;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import local.pms.authservice.kafka.saga.producer.user.UserDetailsCreationProducer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreatedListener {

    final UserDetailsCreationProducer userDetailsCreationProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDetailsCreatedEvent(UserDetailsCreatedEvent event) {
         userDetailsCreationProducer.sendUserDetailsToCreate(KafkaTopics.USER_DETAILS_CREATED_TOPIC, event);
    }
}
