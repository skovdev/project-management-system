package local.pms.organizationservice.exception.handler;

import local.pms.organizationservice.dto.api.response.ApiResponseDto;

import local.pms.organizationservice.exception.LastOwnerRemovalException;
import local.pms.organizationservice.exception.DuplicateMembershipException;
import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;
import local.pms.organizationservice.exception.OrganizationMemberNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the organization-service providing consistent error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrganizationNotFoundException.class)
    public ApiResponseDto<Void> handleOrganizationNotFoundException(OrganizationNotFoundException ex) {
        log.error("Organization not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("ORGANIZATION_NOT_FOUND")
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(OrganizationAccessDeniedException.class)
    public ApiResponseDto<Void> handleOrganizationAccessDeniedException(OrganizationAccessDeniedException ex) {
        log.error("Organization access denied: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                List.of("ORGANIZATION_ACCESS_DENIED")
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrganizationMemberNotFoundException.class)
    public ApiResponseDto<Void> handleOrganizationMemberNotFoundException(OrganizationMemberNotFoundException ex) {
        log.error("Organization member not found: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of("ORGANIZATION_MEMBER_NOT_FOUND")
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateMembershipException.class)
    public ApiResponseDto<Void> handleDuplicateMembershipException(DuplicateMembershipException ex) {
        log.error("Duplicate membership: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                List.of("DUPLICATE_MEMBERSHIP")
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LastOwnerRemovalException.class)
    public ApiResponseDto<Void> handleLastOwnerRemovalException(LastOwnerRemovalException ex) {
        log.error("Last owner removal rejected: {}", ex.getMessage());
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                List.of("LAST_OWNER_REMOVAL")
        );
    }

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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponseDto<Void> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponseDto.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                List.of("INTERNAL_SERVER_ERROR")
        );
    }
}
