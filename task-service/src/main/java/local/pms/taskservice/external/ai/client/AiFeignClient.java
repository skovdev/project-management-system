package local.pms.taskservice.external.ai.client;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the ai-service task acceptance-criteria endpoint.
 */
@FeignClient(name = "ai-service", url = "${project-management-system.client.aiservice.url}")
public interface AiFeignClient {

    /**
     * Sends task context to the ai-service and returns generated acceptance criteria.
     *
     * @param request the task context (title and description)
     * @return API response wrapping the generated acceptance criteria text
     */
    @PostMapping("/acceptance-criteria")
    ApiResponseDto<String> generateAcceptanceCriteria(@RequestBody AcceptanceCriteriaRequestDto request);
}
