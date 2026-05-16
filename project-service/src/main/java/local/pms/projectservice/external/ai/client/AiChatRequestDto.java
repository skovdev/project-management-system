package local.pms.projectservice.external.ai.client;

import com.openai.models.chat.completions.ChatCompletionMessageParam;

import java.util.List;

public record AiChatRequestDto(List<ChatCompletionMessageParam> messages) {}
