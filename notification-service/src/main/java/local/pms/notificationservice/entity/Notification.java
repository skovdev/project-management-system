package local.pms.notificationservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import local.pms.notificationservice.type.NotificationTypeType;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "project_management_system_notification")
@SQLRestriction(value = "deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_notification SET deleted = true WHERE id = ?")
public class Notification extends AbstractBaseModel {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationTypeType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "read", nullable = false)
    private boolean read = false;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
