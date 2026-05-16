package local.pms.projectservice.external.ai.client;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ai-service", url = "${project-management-system.client.aiservice.url}")
public interface AiFeignClient {
    @PostMapping("/ask")
    ApiResponseDto<String> generateProjectDescription(@RequestBody AiChatRequestDto request);
}
