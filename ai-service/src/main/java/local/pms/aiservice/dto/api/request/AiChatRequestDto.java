package local.pms.aiservice.dto.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for the AI chat endpoint.
 * Both fields are provider-agnostic plain strings — no SDK types are exposed.
 */
@Schema(description = "Request payload for AI chat completion")
public record AiChatRequestDto(

        @NotBlank(message = "System prompt must not be blank")
        @Schema(description = "Instruction that sets the AI model's behaviour and persona")
        String systemPrompt,

        @NotBlank(message = "User prompt must not be blank")
        @Schema(description = "User's request or context for the AI model to act on")
        String userPrompt

) {}
