package local.pms.aiservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.aiservice.dto.api.request.task.AcceptanceCriteriaRequestDto;
import local.pms.aiservice.dto.api.request.task.CommentSuggestionsRequestDto;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.service.TaskAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static local.pms.aiservice.constant.VersionAPI.API_V1;

/**
 * REST controller exposing task-related AI generation endpoints.
 * All prompt logic is owned by {@link TaskAiService}; callers supply only domain data.
 */
@Slf4j
@RestController
@RequestMapping(API_V1 + "/ai/task")
@RequiredArgsConstructor
@Tag(name = "Task AI", description = "AI-powered task generation endpoints")
public class TaskAiRestController {

    private final TaskAiService taskAiService;

    /**
     * Generates acceptance criteria for a task from its title and description.
     *
     * @param request the task context (title and description)
     * @return API response wrapping the generated acceptance criteria text
     */
    @Operation(summary = "Generate acceptance criteria", description = "Generates testable acceptance criteria for the given task title and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acceptance criteria generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request — blank title or description"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Failed to communicate with AI model")
    })
    @PostMapping("/acceptance-criteria")
    public ApiResponseDto<String> generateAcceptanceCriteria(@Valid @RequestBody AcceptanceCriteriaRequestDto request) {
        log.info("Received acceptance-criteria generation request for task title='{}'", request.title());
        var result = taskAiService.generateAcceptanceCriteria(request.title(), request.description());
        return ApiResponseDto.buildSuccessResponse(result);
    }

    /**
     * Generates 3 reply suggestions for a task comment from its content and task/thread context.
     *
     * @param request the comment and task context
     * @return API response wrapping exactly 3 AI-generated reply suggestions
     */
    @Operation(summary = "Generate comment reply suggestions", description = "Generates 3 concise, natural, and distinct reply suggestions for the given comment, using the task and thread context")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reply suggestions generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request — blank task title, description, or comment content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Failed to communicate with AI model")
    })
    @PostMapping("/comment-suggestions")
    public ApiResponseDto<List<String>> generateCommentSuggestions(@Valid @RequestBody CommentSuggestionsRequestDto request) {
        log.info("Received comment-suggestions generation request for task title='{}'", request.taskTitle());
        var result = taskAiService.generateCommentSuggestions(
                request.taskTitle(), request.taskDescription(), request.commentContent(), request.threadContext());
        return ApiResponseDto.buildSuccessResponse(result);
    }
}
