package local.pms.aiservice.dto.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;

import java.time.Instant;

import java.util.List;

@Schema(description = "Standard API response envelope for all endpoints")
public record ApiResponseDto<T>(

        @Schema(description = "UTC timestamp of the response") Instant timestamp,
        @Schema(description = "HTTP status code", example = "200") int status,
        @Schema(description = "Human-readable result summary", example = "Request was successful") String message,
        @Schema(description = "Response payload; null on error") T data,
        @Schema(description = "Validation or error details; null on success") List<String> errors,
        @Schema(description = "Machine-readable error code for client branching; null on success", example = "VALIDATION_ERROR") String errorCode

) {

    public static <T> ApiResponseDto<T> buildSuccessResponse(T data) {
        return new ApiResponseDto<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Request was successful",
                data,
                null,
                null
        );
    }

    public static <T> ApiResponseDto<T> buildErrorResponse(int status, String message, List<String> errors, String errorCode) {
        return new ApiResponseDto<>(
                Instant.now(),
                status,
                message,
                null,
                errors,
                errorCode
        );
    }
}
