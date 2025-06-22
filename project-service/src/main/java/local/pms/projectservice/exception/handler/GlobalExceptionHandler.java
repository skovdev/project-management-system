package local.pms.projectservice.exception.handler;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.InvalidProjectInputException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProjectNotFoundException.class)
    public ApiResponseDto<Void> handleProjectNotFoundException(ProjectNotFoundException ex) {
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        List.of("Project not found")
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidProjectInputException.class)
    public ApiResponseDto<Void> handleInvalidProjectInputException(InvalidProjectInputException ex) {
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        List.of("Invalid project input")
                );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DescriptionGenerationException.class)
    public ApiResponseDto<Void> handleDescriptionGenerationException(DescriptionGenerationException ex) {
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        List.of("Error generating project description")
                );
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponseDto<Void> handleGenericException(Exception ex) {
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred",
                        List.of(ex.getMessage())
                );
    }
}
