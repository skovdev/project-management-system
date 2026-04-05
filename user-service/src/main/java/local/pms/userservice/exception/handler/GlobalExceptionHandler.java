package local.pms.userservice.exception.handler;

import local.pms.userservice.dto.api.response.ApiResponseDto;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler that maps domain exceptions to HTTP responses
 * using the standard {@link ApiResponseDto} envelope.
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
     * Handles {@link UserNotFoundException} and returns HTTP 404.
     *
     * @param ex the exception
     * @return error response with NOT_FOUND status
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ApiResponseDto<Void> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("USER_NOT_FOUND")
        );
    }

    /**
     * Handles {@link UserAccessDeniedException} and returns HTTP 403.
     *
     * @param ex the exception
     * @return error response with FORBIDDEN status
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserAccessDeniedException.class)
    public ApiResponseDto<Void> handleUserAccessDeniedException(UserAccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                List.of("USER_ACCESS_DENIED")
        );
    }

    /**
     * Handles all unhandled exceptions and returns HTTP 500.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
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
