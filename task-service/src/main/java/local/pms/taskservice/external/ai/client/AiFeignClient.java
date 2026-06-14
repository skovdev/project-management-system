package local.pms.taskservice.external.ai.client;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the ai-service chat completion endpoint.
 */
@FeignClient(name = "ai-service", url = "${project-management-system.client.aiservice.url}")
public interface AiFeignClient {

    /**
     * Sends a list of messages to the ai-service and returns the generated text.
     *
     * @param request the chat request containing the message list
     * @return API response wrapping the generated string
     */
    @PostMapping("/ask")
    ApiResponseDto<String> generateContent(@RequestBody AiChatRequestDto request);
}
