package local.pms.aiservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.aiservice.dto.api.request.task.AcceptanceCriteriaRequestDto;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.service.TaskAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/acceptance-criteria")
    public ApiResponseDto<String> generateAcceptanceCriteria(@Valid @RequestBody AcceptanceCriteriaRequestDto request) {
        log.info("Received acceptance-criteria generation request for task title='{}'", request.title());
        var result = taskAiService.generateAcceptanceCriteria(request.title(), request.description());
        return ApiResponseDto.buildSuccessResponse(result);
    }
}
