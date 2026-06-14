package local.pms.taskservice.external.ai.client;

import com.openai.models.chat.completions.ChatCompletionMessageParam;

import java.util.List;

/**
 * Request payload sent to the ai-service chat endpoint.
 */
public record AiChatRequestDto(List<ChatCompletionMessageParam> messages) {}
