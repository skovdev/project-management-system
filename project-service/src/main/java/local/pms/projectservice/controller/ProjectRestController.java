package local.pms.projectservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.projectservice.dto.ProjectDto;
import local.pms.projectservice.dto.ProjectOrganizationDto;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.service.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static local.pms.projectservice.constant.VersionAPI.API_V1;

@Tag(name = "Project", description = "Project REST API")
@RestController
@RequestMapping(API_V1 + "/projects")
@RequiredArgsConstructor
public class ProjectRestController {

    private final ProjectService projectService;

    @Operation(summary = "Create a new project", description = "The caller must be an OWNER or ADMIN member of the given organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid project data provided"),
            @ApiResponse(responseCode = "500", description = "Error occurred while creating project"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> create(@Parameter(description = "Project data to create a new project")
                                             @Valid @RequestBody ProjectDto projectDto) {
        return ApiResponseDto.buildSuccessResponse(projectService.create(projectDto));
    }

    @Operation(
            summary = "Find all projects in an organization",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc). The caller must be a member of the organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProjectDto> findAll(@Parameter(description = "Organization identifier to scope the list to")
                                    @RequestParam(name = "organizationId") UUID organizationId,
                                    @ParameterObject Pageable pageable) {
        return projectService.findAll(organizationId, pageable);
    }

    @Operation(summary = "Find a project by project identifier", description = "The caller must be a member of the project's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project details retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> findById(@Parameter(description = "Project identifier to retrieve details")
                                               @PathVariable(name = "projectId") UUID projectId,
                                               @Parameter(description = "Organization the project must belong to")
                                               @RequestParam(name = "organizationId") UUID organizationId) {
        return ApiResponseDto.buildSuccessResponse(projectService.findById(projectId, organizationId));
    }

    @Operation(summary = "Update an existing project", description = "The caller must be an OWNER or ADMIN member of the project's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid project data provided"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> update(@Parameter(description = "Project identifier to update")
                                             @PathVariable(name = "projectId") UUID projectId,
                                             @Parameter(description = "Updated project data")
                                             @Valid @RequestBody ProjectDto projectDto) {
        return ApiResponseDto.buildSuccessResponse(projectService.update(projectId, projectDto));
    }

    @Operation(summary = "Delete a project by project identifier", description = "The caller must be an OWNER or ADMIN member of the project's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(@Parameter(description = "Project identifier to delete")
                                       @PathVariable(name = "projectId") UUID projectId,
                                       @Parameter(description = "Organization the project must belong to")
                                       @RequestParam(name = "organizationId") UUID organizationId) {
        projectService.delete(projectId, organizationId);
        return ApiResponseDto.buildSuccessResponse(null);
    }

    @Operation(summary = "Generate project description", description = "The caller must be a member of the project's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project description generated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            }),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid project title provided"),
            @ApiResponse(responseCode = "500", description = "Error occurred while generating project description"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(value = "/{projectId}/description", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<String> generateProjectDescription(@Parameter(description = "Project identifier to generate project description")
                                                             @PathVariable(name = "projectId") UUID projectId,
                                                             @Parameter(description = "Organization the project must belong to")
                                                             @RequestParam(name = "organizationId") UUID organizationId,
                                                             @Parameter(description = "Project title to generate project description")
                                                             @RequestParam(name = "projectTitle") String projectTitle) {
        return ApiResponseDto.buildSuccessResponse(projectService.generateProjectDescription(projectId, organizationId, projectTitle));
    }

    @Operation(
            summary = "Resolve the organization a project belongs to",
            description = "Used by other services to derive organizationId from a projectId. The caller must be a member of the project's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization identifier resolved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectOrganizationDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied — not a member of the project's organization"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping(value = "/{projectId}/organization-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectOrganizationDto> findOrganizationIdByProjectId(@Parameter(description = "Project identifier to resolve the organization for")
                                                                                @PathVariable(name = "projectId") UUID projectId) {
        return ApiResponseDto.buildSuccessResponse(
                new ProjectOrganizationDto(projectService.findOrganizationIdByProjectId(projectId)));
    }
}
