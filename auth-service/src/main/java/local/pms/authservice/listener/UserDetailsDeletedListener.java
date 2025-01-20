package local.pms.authservice.listener;

import local.pms.authservice.constant.KafkaTopics;

import local.pms.authservice.event.UserDetailsDeletedEvent;

import local.pms.authservice.kafka.saga.producer.user.UserDetailsDeletionProducer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserDetailsDeletedListener {

    private final UserDetailsDeletionProducer userDetailsDeletionProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDetailsDeletedEvent(UserDetailsDeletedEvent event) {
        userDetailsDeletionProducer.sendUserDetailsToDelete(KafkaTopics.USER_DETAILS_DELETED_TOPIC, event.authUserId());
    }
}