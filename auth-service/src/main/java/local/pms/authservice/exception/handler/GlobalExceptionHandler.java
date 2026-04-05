package local.pms.authservice.exception.handler;

import local.pms.authservice.dto.api.response.ApiResponseDto;

import local.pms.authservice.exception.AuthUserNotFoundException;
import local.pms.authservice.exception.AuthUsernameAlreadyExistsException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the auth-service providing consistent error responses.
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
    public ApiResponseDto<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
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
     * Handles {@link AuthUsernameAlreadyExistsException} and returns a 400 error response.
     *
     * @param ex the exception thrown when a username is already taken
     * @return error response with BAD_REQUEST status
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthUsernameAlreadyExistsException.class)
    public ApiResponseDto<String> handleUsernameAlreadyExistsException(AuthUsernameAlreadyExistsException ex) {
        log.warn("Username already exists: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                List.of("USERNAME_ALREADY_EXISTS")
        );
    }

    /**
     * Handles {@link AuthUserNotFoundException} and returns a 404 error response.
     *
     * @param ex the exception thrown when the requested auth user is not found
     * @return error response with NOT_FOUND status
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AuthUserNotFoundException.class)
    public ApiResponseDto<String> handleAuthenticationUserNotFoundException(AuthUserNotFoundException ex) {
        log.warn("Auth user not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("AUTH_USER_NOT_FOUND")
        );
    }

    /**
     * Handles {@link BadCredentialsException} and returns a 401 error response.
     *
     * @param ex the exception thrown when credentials are invalid
     * @return error response with UNAUTHORIZED status
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponseDto<String> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                List.of("BAD_CREDENTIALS")
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
    public ApiResponseDto<String> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                List.of("INTERNAL_SERVER_ERROR")
        );
    }
}
