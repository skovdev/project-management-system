package local.pms.taskservice.external.ai.client;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Feign client for the ai-service task acceptance-criteria and comment-suggestions endpoints.
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

    /**
     * Sends comment and task/thread context to the ai-service and returns 3 generated reply suggestions.
     *
     * @param request the comment and task context
     * @return API response wrapping the generated reply suggestions
     */
    @PostMapping("/comment-suggestions")
    ApiResponseDto<List<String>> generateCommentSuggestions(@RequestBody CommentSuggestionsRequestDto request);
}
