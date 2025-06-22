package local.pms.projectservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.service.ProjectService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static local.pms.projectservice.constant.VersionAPI.API_V1;

@Tag(name = "Project", description = "Project REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/projects")
@RequiredArgsConstructor
public class ProjectRestController {

    final ProjectService projectService;

    @Operation(summary = "Find all projects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            }),
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProjectDto> findAll(@Parameter(description = "This parameter contains the page number")
                                    @RequestParam(defaultValue = "0") int page,
                                    @Parameter(description = "This parameter contains the number of elements per page")
                                    @RequestParam(defaultValue = "10") int size,
                                    @Parameter(description = "This parameter contains the field name to sort")
                                    @RequestParam(defaultValue = "id") String sortBy,
                                    @Parameter(description = "This parameter contains the sort order")
                                    @RequestParam(defaultValue = "asc") String order) {
        return projectService.findAll(page, size, sortBy, order);
    }

    @Operation(summary = "Generate project description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project description generated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            }),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid project title provided"),
            @ApiResponse(responseCode = "500", description = "Error occurred while generating project description"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping(value = "/{projectId}/description", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<String> generateProjectDescription(@Parameter(description = "Project identifier to generate project description")
                                             @PathVariable(name = "projectId") UUID projectId,
                                                     @Parameter(description = "Project title to generate project description")
                                             @RequestParam(name = "projectTitle") String projectTitle) {
        return ApiResponseDto.buildSuccessResponse(projectService.generateProjectDescription(projectId, projectTitle));
    }
}
