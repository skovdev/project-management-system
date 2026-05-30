package local.pms.notificationservice.exception;

import local.pms.notificationservice.dto.api.response.ApiResponseDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the notification-service providing consistent error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from {@code @Valid}-annotated request bodies.
     *
     * @param ex the exception containing binding result with field errors
     * @return 400 response with field violation details
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponseDto<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", fieldErrors);
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for " + fieldErrors.size() + " field(s)",
                fieldErrors
        );
    }

    /**
     * Handles {@link NotificationNotFoundException} and returns a 404 response.
     *
     * @param ex the not-found exception
     * @return 404 error response
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotificationNotFoundException.class)
    public ApiResponseDto<Void> handleNotificationNotFoundException(NotificationNotFoundException ex) {
        log.error("Notification not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("NOTIFICATION_NOT_FOUND")
        );
    }

    /**
     * Handles {@link NotificationAccessDeniedException} and returns a 403 response.
     *
     * @param ex the access-denied exception
     * @return 403 error response
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ApiResponseDto<Void> handleNotificationAccessDeniedException(NotificationAccessDeniedException ex) {
        log.error("Notification access denied: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                List.of("NOTIFICATION_ACCESS_DENIED")
        );
    }

    /**
     * Handles Spring Security {@link AccessDeniedException} from {@code @PreAuthorize} and returns 403.
     *
     * @param ex the access denied exception
     * @return 403 error response
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponseDto<Void> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                List.of("ACCESS_DENIED")
        );
    }

    /**
     * Fallback handler for any unhandled exception; returns a 500 response.
     *
     * @param ex the unexpected exception
     * @return 500 error response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponseDto<Void> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                List.of("INTERNAL_SERVER_ERROR")
        );
    }
}
