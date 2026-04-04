package local.pms.userservice.exception.handler;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that maps domain exceptions to HTTP responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link UserNotFoundException} and returns HTTP 404.
     *
     * @param ex the exception
     * @return 404 response with the error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles {@link UserAccessDeniedException} and returns HTTP 403.
     *
     * @param ex the exception
     * @return 403 response with the error message
     */
    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<String> handleUserAccessDeniedException(UserAccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Handles all unhandled exceptions and returns HTTP 500.
     *
     * @param ex the exception
     * @return 500 response with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }
}
