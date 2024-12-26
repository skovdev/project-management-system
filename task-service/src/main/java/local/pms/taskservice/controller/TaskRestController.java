package local.pms.taskservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static local.pms.taskservice.constant.VersionAPI.API_V1;

@Tag(name = "Task", description = "Task REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/tasks")
@RequiredArgsConstructor
public class TaskRestController {

    final TaskService taskService;

    @Operation(summary = "Find all tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            })
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TaskDto> findAll(@Parameter(description = "This parameter contains the page number")
                                 @RequestParam(defaultValue = "0") int page,
                                 @Parameter(description = "This parameter contains the number of elements per page")
                                 @RequestParam(defaultValue = "10") int size,
                                 @Parameter(description = "This parameter contains the field name to sort")
                                 @RequestParam(defaultValue = "id") String sortBy,
                                 @Parameter(description = "This parameter contains the sort order")
                                 @RequestParam(defaultValue = "asc") String order) {
        return taskService.findAll(page, size, sortBy, order);
    }
}
