package local.pms.taskservice.exception;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the task-service providing consistent error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when {@code @Valid} fails on a request body.
     * Collects all field-level constraint violations and returns them as a 400 error response.
     *
     * @param ex the exception containing binding result with field errors
     * @return error response with BAD_REQUEST status and a list of field violation messages
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
     * Handles {@link TaskAccessDeniedException} and returns a 403 error response.
     *
     * @param ex the exception thrown when a user attempts to access another user's task
     * @return error response with FORBIDDEN status
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(TaskAccessDeniedException.class)
    public ApiResponseDto<Void> handleTaskAccessDeniedException(TaskAccessDeniedException ex) {
        log.error("Task access denied: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                List.of("TASK_ACCESS_DENIED"));
    }

    /**
     * Handles {@link TaskNotFoundException} and returns a 404 error response.
     *
     * @param ex the exception containing the not-found message
     * @return error response with NOT_FOUND status
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TaskNotFoundException.class)
    public ApiResponseDto<Void> handleTaskNotFoundException(TaskNotFoundException ex) {
        log.error("Task not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("TASK_NOT_FOUND"));
    }

    /**
     * Handles {@link InvalidTaskInputException} and returns a 400 error response.
     *
     * @param ex the exception containing the validation failure message
     * @return error response with BAD_REQUEST status
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTaskInputException.class)
    public ApiResponseDto<Void> handleInvalidTaskInputException(InvalidTaskInputException ex) {
        log.error("Invalid task input: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                List.of("INVALID_TASK_INPUT"));
    }

    /**
     * Handles Spring Security {@link AccessDeniedException} thrown by {@code @PreAuthorize}
     * method-level security and returns a 403 error response.
     *
     * @param ex the access denied exception
     * @return error response with FORBIDDEN status
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponseDto<Void> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied",
                        List.of("ACCESS_DENIED")
                );
    }

    /**
     * Handles any unhandled {@link Exception} and returns a 500 error response.
     *
     * @param ex the unexpected exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponseDto<Void> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                List.of("INTERNAL_SERVER_ERROR"));
    }
}