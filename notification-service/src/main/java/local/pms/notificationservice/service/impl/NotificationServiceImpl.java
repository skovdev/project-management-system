package local.pms.notificationservice.service.impl;

import local.pms.notificationservice.config.jwt.JwtTokenProvider;

import local.pms.notificationservice.dto.NotificationDto;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.exception.NotificationAccessDeniedException;
import local.pms.notificationservice.exception.NotificationNotFoundException;

import local.pms.notificationservice.mapping.NotificationMapping;

import local.pms.notificationservice.repository.NotificationRepository;

import local.pms.notificationservice.service.NotificationService;
import local.pms.notificationservice.service.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing user notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapping notificationMapping = NotificationMapping.INSTANCE;

    private final NotificationRepository notificationRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> findAll(Pageable pageable) {
        return notificationRepository.findAllByUserId(extractAuthUserId(), pageable)
                .map(notificationMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> findUnread() {
        return notificationRepository.findTop5ByUserIdAndReadFalseOrderByCreatedAtDesc(extractAuthUserId())
                .stream()
                .map(notificationMapping::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto findById(UUID notificationId) {
        var notification = notificationRepository.findByIdAndUserId(notificationId, extractAuthUserId());
        if (notification.isEmpty()) {
            log.error("Notification with ID {} not found or access denied.", notificationId);
            throw new NotificationNotFoundException("Notification with ID " + notificationId + " not found. Please provide a valid notification ID");
        }
        return notificationMapping.toDto(notification.get());
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(UUID notificationId) {
        UUID authUserId = extractAuthUserId();
        var existing = notificationRepository.findById(notificationId);
        if (existing.isEmpty()) {
            log.error("Notification with ID {} not found, cannot mark as read.", notificationId);
            throw new NotificationNotFoundException("Notification with ID " + notificationId + " not found. Please provide a valid notification ID");
        }
        if (!existing.get().getUserId().equals(authUserId)) {
            log.error("User {} attempted to mark notification {} owned by another user.", authUserId, notificationId);
            throw new NotificationAccessDeniedException("Access denied: you do not own notification with ID " + notificationId);
        }
        existing.get().setRead(true);
        var updated = notificationRepository.save(existing.get());
        log.info("Notification with ID {} marked as read for user {}.", notificationId, authUserId);
        return notificationMapping.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID notificationId) {
        UUID authUserId = extractAuthUserId();
        var existing = notificationRepository.findById(notificationId);
        if (existing.isEmpty()) {
            log.error("Notification with ID {} not found, cannot delete.", notificationId);
            throw new NotificationNotFoundException("Notification with ID " + notificationId + " not found. Please provide a valid notification ID");
        }
        if (!existing.get().getUserId().equals(authUserId)) {
            log.error("User {} attempted to delete notification {} owned by another user.", authUserId, notificationId);
            throw new NotificationAccessDeniedException("Access denied: you do not own notification with ID " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
        log.info("Notification with ID {} deleted successfully.", notificationId);
    }

    @Override
    @Transactional
    public void save(Notification notification) {
        notificationRepository.save(notification);
        log.info("Notification of type {} saved for userId {}.", notification.getType(), notification.getUserId());
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract authenticated user ID.");
            throw new NotificationAccessDeniedException("Access denied: missing or invalid authentication token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}
