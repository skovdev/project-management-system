package local.pms.aiservice.dto.api.request;

import com.openai.models.chat.completions.ChatCompletionMessageParam;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request payload for ChatGPT conversation")
public record ChatGptRequestDto(

        @NotNull(message = "Messages list must not be null")
        @NotEmpty(message = "Messages list must not be empty")
        @Schema(description = "List of conversation messages to send to ChatGPT")
        List<ChatCompletionMessageParam> messages

) {}
