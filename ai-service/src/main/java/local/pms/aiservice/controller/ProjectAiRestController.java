package local.pms.aiservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.aiservice.dto.api.request.project.ProjectDescriptionRequestDto;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.service.ProjectAiService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static local.pms.aiservice.constant.VersionAPI.API_V1;

/**
 * REST controller exposing project-related AI generation endpoints.
 * All prompt logic is owned by {@link ProjectAiService}; callers supply only domain data.
 */
@Slf4j
@RestController
@RequestMapping(API_V1 + "/ai/project")
@RequiredArgsConstructor
@Tag(name = "Project AI", description = "AI-powered project generation endpoints")
public class ProjectAiRestController {

    private final ProjectAiService projectAiService;

    /**
     * Generates a project description from a project title.
     *
     * @param request the project context (title)
     * @return API response wrapping the generated description text
     */
    @Operation(summary = "Generate project description", description = "Generates a concise project description from the given project title")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project description generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request — blank title"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Failed to communicate with AI model")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/description")
    public ApiResponseDto<String> generateProjectDescription(@Valid @RequestBody ProjectDescriptionRequestDto request) {
        log.info("Received project-description generation request for title='{}'", request.title());
        var result = projectAiService.generateProjectDescription(request.title());
        return ApiResponseDto.buildSuccessResponse(result);
    }
}
