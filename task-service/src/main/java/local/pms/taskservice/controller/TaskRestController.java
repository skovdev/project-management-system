package local.pms.taskservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.taskservice.dto.TaskDto;
import local.pms.taskservice.dto.api.response.ApiResponseDto;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static local.pms.taskservice.constant.VersionAPI.API_V1;

@Tag(name = "Task", description = "Task REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/tasks")
@RequiredArgsConstructor
public class TaskRestController {

    final TaskService taskService;

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid task data provided"),
            @ApiResponse(responseCode = "500", description = "Error occurred while creating task"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<TaskDto> create(@Parameter(description = "Task data to create a new task")
                                          @Valid @RequestBody TaskDto taskDto) {
        return ApiResponseDto.buildSuccessResponse(taskService.create(taskDto));
    }

    @Operation(
            summary = "Find all tasks",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc). " +
                          "When projectId is provided, returns all tasks in that project (caller must be a member " +
                          "of the project's organization); otherwise returns only the caller's own tasks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TaskDto> findAll(@Parameter(description = "Project identifier to scope the list to")
                                 @RequestParam(name = "projectId", required = false) UUID projectId,
                                 @ParameterObject Pageable pageable) {
        return projectId != null ? taskService.findAllByProject(projectId, pageable) : taskService.findAll(pageable);
    }

    @Operation(summary = "Find a task by task identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task details retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<TaskDto> findById(@Parameter(description = "Task identifier to retrieve details")
                                            @PathVariable(name = "taskId") UUID taskId) {
        return ApiResponseDto.buildSuccessResponse(taskService.findById(taskId));
    }

    @Operation(summary = "Update an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid task data provided"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<TaskDto> update(@Parameter(description = "Task identifier to update")
                                          @PathVariable(name = "taskId") UUID taskId,
                                          @Parameter(description = "Updated task data")
                                          @Valid @RequestBody TaskDto taskDto) {
        return ApiResponseDto.buildSuccessResponse(taskService.update(taskId, taskDto));
    }

    @Operation(summary = "Delete a task by task identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(@Parameter(description = "Task identifier to delete")
                                       @PathVariable(name = "taskId") UUID taskId) {
        taskService.delete(taskId);
        return ApiResponseDto.buildSuccessResponse(null);
    }

    @Operation(
            summary = "Generate acceptance criteria",
            description = "Uses AI to generate acceptance criteria based on the task title and description. " +
                          "The result is returned to the client and not persisted automatically. " +
                          "Call PUT /{taskId} with the acceptanceCriteria field to save.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acceptance criteria generated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Error occurred while generating acceptance criteria")
    })
    @PostMapping(value = "/{taskId}/acceptance-criteria", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<String> generateAcceptanceCriteria(
            @Parameter(description = "Task identifier for which to generate acceptance criteria")
            @PathVariable(name = "taskId") UUID taskId) {
        return ApiResponseDto.buildSuccessResponse(taskService.generateAcceptanceCriteria(taskId));
    }
}
