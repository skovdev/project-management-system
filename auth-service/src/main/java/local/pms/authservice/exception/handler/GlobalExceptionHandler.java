package local.pms.authservice.exception.handler;

import local.pms.authservice.dto.api.response.ApiResponseDto;

import local.pms.authservice.exception.AuthUserNotFoundException;
import local.pms.authservice.exception.AuthUsernameAlreadyExistsException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthUsernameAlreadyExistsException.class)
    public ApiResponseDto<String> handleUsernameAlreadyExistsException(AuthUsernameAlreadyExistsException ex) {
        return ApiResponseDto.buildErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        List.of("The username is already taken. Please choose a different username.")
                );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AuthUserNotFoundException.class)
    public ApiResponseDto<String> handleAuthenticationUserNotFoundException(AuthUserNotFoundException ex) {
        return ApiResponseDto.buildErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        List.of("The requested user was not found. Please check the user ID and try again.")
                );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponseDto<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ApiResponseDto.buildErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage(),
                        List.of("Please check your credentials and try again.")
                );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponseDto<String> handleGeneralException(Exception ex) {
        return ApiResponseDto.buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        List.of("An unexpected error occurred. Please try again later.")
                );
    }
}