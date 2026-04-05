package local.pms.projectservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.service.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

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

    @Operation(summary = "Create a new project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid project data provided"),
            @ApiResponse(responseCode = "500", description = "Error occurred while creating project"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> create(@Parameter(description = "Project data to create a new project")
                                             @Valid @RequestBody ProjectDto projectDto) {
        return ApiResponseDto.buildSuccessResponse(projectService.create(projectDto));
    }

    @Operation(
            summary = "Find all projects",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ProjectDto> findAll(@ParameterObject Pageable pageable) {
        return projectService.findAll(pageable);
    }

    @Operation(summary = "Find a project by project identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project details retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> findById(@Parameter(description = "Project identifier to retrieve details")
                                               @PathVariable(name = "projectId") UUID projectId) {
        return ApiResponseDto.buildSuccessResponse(projectService.findById(projectId));
    }

    @Operation(summary = "Update an existing project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProjectDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid project data provided"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<ProjectDto> update(@Parameter(description = "Project identifier to update")
                                             @PathVariable(name = "projectId") UUID projectId,
                                             @Parameter(description = "Updated project data")
                                             @Valid @RequestBody ProjectDto projectDto) {
        return ApiResponseDto.buildSuccessResponse(projectService.update(projectId, projectDto));
    }

    @Operation(summary = "Delete a project by project identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(@Parameter(description = "Project identifier to delete")
                                       @PathVariable(name = "projectId") UUID projectId) {
        projectService.delete(projectId);
        return ApiResponseDto.buildSuccessResponse(null);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/{projectId}/description", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<String> generateProjectDescription(@Parameter(description = "Project identifier to generate project description")
                                                             @PathVariable(name = "projectId") UUID projectId,
                                                             @Parameter(description = "Project title to generate project description")
                                                             @RequestParam(name = "projectTitle") String projectTitle) {
        return ApiResponseDto.buildSuccessResponse(projectService.generateProjectDescription(projectId, projectTitle));
    }
}