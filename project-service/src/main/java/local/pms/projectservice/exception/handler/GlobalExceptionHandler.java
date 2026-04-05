package local.pms.projectservice.exception.handler;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the project-service providing consistent error responses.
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
     * Handles {@link ProjectAccessDeniedException} and returns a 403 error response.
     *
     * @param ex the exception thrown when a user attempts to access another user's project
     * @return error response with FORBIDDEN status
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ApiResponseDto<Void> handleProjectAccessDeniedException(ProjectAccessDeniedException ex) {
        log.error("Project access denied: {}", ex.getMessage());
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage(),
                        List.of("PROJECT_ACCESS_DENIED")
                );
    }

    /**
     * Handles {@link ProjectNotFoundException} and returns a 404 error response.
     *
     * @param ex the exception containing the not-found message
     * @return error response with NOT_FOUND status
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProjectNotFoundException.class)
    public ApiResponseDto<Void> handleProjectNotFoundException(ProjectNotFoundException ex) {
        log.error("Project not found: {}", ex.getMessage());
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        List.of("PROJECT_NOT_FOUND")
                );
    }

    /**
     * Handles {@link InvalidProjectInputException} and returns a 400 error response.
     *
     * @param ex the exception containing the validation failure message
     * @return error response with BAD_REQUEST status
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidProjectInputException.class)
    public ApiResponseDto<Void> handleInvalidProjectInputException(InvalidProjectInputException ex) {
        log.error("Invalid project input: {}", ex.getMessage());
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        List.of("INVALID_PROJECT_INPUT")
                );
    }

    /**
     * Handles {@link DescriptionGenerationException} and returns a 500 error response.
     *
     * @param ex the exception containing the generation failure message
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DescriptionGenerationException.class)
    public ApiResponseDto<Void> handleDescriptionGenerationException(DescriptionGenerationException ex) {
        log.error("Project description generation failed: {}", ex.getMessage());
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        List.of("DESCRIPTION_GENERATION_FAILED")
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
    public ApiResponseDto<Void> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred",
                        List.of("INTERNAL_SERVER_ERROR")
                );
    }
}
