package local.pms.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.notificationservice.dto.NotificationDto;

import local.pms.notificationservice.dto.api.response.ApiResponseDto;

import local.pms.notificationservice.service.NotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static local.pms.notificationservice.constant.VersionAPI.API_V1;

@Tag(name = "Notification", description = "Notification REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    final NotificationService notificationService;

    @Operation(summary = "Find all notifications for the authenticated user",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of notifications", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotificationDto.class))
            })
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<NotificationDto> findAll(@ParameterObject Pageable pageable) {
        return notificationService.findAll(pageable);
    }

    @Operation(summary = "Find the five most recent unread notifications",
            description = "Used to populate the header bell dropdown. Returns up to 5 unread notifications ordered newest-first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of unread notifications", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotificationDto.class))
            })
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/unread", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<List<NotificationDto>> findUnread() {
        return ApiResponseDto.buildSuccessResponse(notificationService.findUnread());
    }

    @Operation(summary = "Find a notification by identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification details", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotificationDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/{notificationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<NotificationDto> findById(
            @Parameter(description = "Notification identifier")
            @PathVariable UUID notificationId) {
        return ApiResponseDto.buildSuccessResponse(notificationService.findById(notificationId));
    }

    @Operation(summary = "Mark a notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotificationDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{notificationId}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<NotificationDto> markAsRead(
            @Parameter(description = "Notification identifier to mark as read")
            @PathVariable UUID notificationId) {
        return ApiResponseDto.buildSuccessResponse(notificationService.markAsRead(notificationId));
    }

    @Operation(summary = "Delete a notification by identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(value = "/{notificationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(
            @Parameter(description = "Notification identifier to delete")
            @PathVariable UUID notificationId) {
        notificationService.delete(notificationId);
        return ApiResponseDto.buildSuccessResponse(null);
    }
}
