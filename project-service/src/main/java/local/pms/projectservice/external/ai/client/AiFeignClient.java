package local.pms.projectservice.external.ai.client;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the ai-service project description endpoint.
 */
@FeignClient(name = "ai-service", url = "${project-management-system.client.aiservice.url}")
public interface AiFeignClient {

    /**
     * Sends project context to the ai-service and returns a generated project description.
     *
     * @param request the project context (title)
     * @return API response wrapping the generated description text
     */
    @PostMapping("/description")
    ApiResponseDto<String> generateProjectDescription(@RequestBody ProjectDescriptionRequestDto request);
}
