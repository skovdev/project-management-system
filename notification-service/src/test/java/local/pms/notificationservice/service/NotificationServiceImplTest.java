package local.pms.notificationservice.service;

import local.pms.notificationservice.config.jwt.JwtTokenProvider;

import local.pms.notificationservice.dto.NotificationDto;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.exception.NotificationAccessDeniedException;
import local.pms.notificationservice.exception.NotificationNotFoundException;

import local.pms.notificationservice.repository.NotificationRepository;

import local.pms.notificationservice.service.impl.NotificationServiceImpl;

import local.pms.notificationservice.type.NotificationTypeType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("findAll returns page of notifications for the authenticated user")
    void should_returnPageOfNotifications_when_findAll() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var notification = buildNotification(userId);
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findAllByUserId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var result = notificationService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).type()).isEqualTo(NotificationTypeType.WELCOME);
    }

    @Test
    @DisplayName("findUnread returns top 5 unread notifications")
    void should_returnUnreadNotifications_when_findUnread() {
        var userId = UUID.randomUUID();
        var notification = buildNotification(userId);
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findTop5ByUserIdAndReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification));

        var result = notificationService.findUnread();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).read()).isFalse();
    }

    @Test
    @DisplayName("findById throws NotificationNotFoundException when notification does not exist")
    void should_throwNotificationNotFoundException_when_notificationNotFound() {
        var userId = UUID.randomUUID();
        var notificationId = UUID.randomUUID();
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.findById(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("markAsRead updates read flag to true for the owner")
    void should_markNotificationAsRead_when_ownerRequests() {
        var userId = UUID.randomUUID();
        var notification = buildNotification(userId);
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        var result = notificationService.markAsRead(notification.getId());

        assertThat(result.read()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead throws NotificationAccessDeniedException when another user tries to mark it")
    void should_throwAccessDenied_when_nonOwnerTriesToMarkAsRead() {
        var ownerId = UUID.randomUUID();
        var requesterId = UUID.randomUUID();
        var notification = buildNotification(ownerId);
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(requesterId);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(notification.getId()))
                .isInstanceOf(NotificationAccessDeniedException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete calls repository deleteById for a valid notification")
    void should_deleteNotification_when_validIdProvided() {
        var userId = UUID.randomUUID();
        var notification = buildNotification(userId);
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        notificationService.delete(notification.getId());

        verify(notificationRepository).deleteById(notification.getId());
    }

    @Test
    @DisplayName("delete throws NotificationNotFoundException when notification does not exist")
    void should_throwNotificationNotFoundException_when_deletingNonExistentNotification() {
        var userId = UUID.randomUUID();
        var notificationId = UUID.randomUUID();
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(userId);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.delete(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).deleteById(any());
    }

    private Notification buildNotification(UUID userId) {
        var notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setType(NotificationTypeType.WELCOME);
        notification.setTitle("Welcome to PMS!");
        notification.setMessage("Hi User, welcome!");
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
