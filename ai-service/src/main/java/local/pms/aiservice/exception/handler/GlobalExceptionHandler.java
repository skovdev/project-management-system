package local.pms.aiservice.exception.handler;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.exception.ChatGptException;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ChatGptException.class)
    public ApiResponseDto<Void> handleChatGptException(ChatGptException ex) {
        return ApiResponseDto
                .buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        List.of("Error communicating with ChatGPT service")
                );
    }
}
