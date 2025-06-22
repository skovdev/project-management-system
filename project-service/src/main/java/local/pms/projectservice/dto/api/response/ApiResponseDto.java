package local.pms.projectservice.dto.api.response;

import lombok.Data;
import lombok.Builder;

import org.springframework.http.HttpStatus;

import java.time.Instant;

import java.util.List;

@Data
@Builder
public class ApiResponseDto<T> {

    private Instant timestamp;
    private int status;
    private String message;
    private T data;
    private List<String> errors;

    public static <T> ApiResponseDto<T> buildSuccessResponse(T data) {
        return ApiResponseDto.<T>builder()
                .timestamp(Instant.now())
                .status(HttpStatus.OK.value())
                .message("Request was successful")
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> buildErrorResponse(int status, String message, List<String> errors) {
        return ApiResponseDto.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }

}
