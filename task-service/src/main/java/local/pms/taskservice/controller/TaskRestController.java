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

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<TaskDto> create(@Parameter(description = "Task data to create a new task")
                                          @RequestBody TaskDto taskDto) {
        return ApiResponseDto.buildSuccessResponse(taskService.create(taskDto));
    }

    @Operation(
            summary = "Find all tasks",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            })
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TaskDto> findAll(@ParameterObject Pageable pageable) {
        return taskService.findAll(pageable);
    }

    @Operation(summary = "Find a task by task identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task details retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<TaskDto> update(@Parameter(description = "Task identifier to update")
                                          @PathVariable(name = "taskId") UUID taskId,
                                          @Parameter(description = "Updated task data")
                                          @RequestBody TaskDto taskDto) {
        return ApiResponseDto.buildSuccessResponse(taskService.update(taskId, taskDto));
    }

    @Operation(summary = "Delete a task by task identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(@Parameter(description = "Task identifier to delete")
                                       @PathVariable(name = "taskId") UUID taskId) {
        taskService.delete(taskId);
        return ApiResponseDto.buildSuccessResponse(null);
    }
}
