package local.pms.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import local.pms.notificationservice.type.NotificationTypeType;

import java.time.Instant;

@Schema(description = "Notification details")
public record NotificationDto(

        @Schema(description = "Notification identifier")
        String id,

        @Schema(description = "Identifier of the user this notification belongs to")
        String userId,

        @Schema(description = "Notification category")
        NotificationTypeType type,

        @Schema(description = "Short notification title")
        String title,

        @Schema(description = "Full notification message")
        String message,

        @Schema(description = "Whether the notification has been read by the user")
        boolean read,

        @Schema(description = "Timestamp when the notification was created")
        Instant createdAt
) {}
