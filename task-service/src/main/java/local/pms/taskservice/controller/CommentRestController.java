package local.pms.taskservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import local.pms.taskservice.service.CommentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static local.pms.taskservice.constant.VersionAPI.API_V1;

/**
 * REST controller for managing comments on tasks.
 */
@Tag(name = "Comment", description = "Task Comment REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentRestController {

    final CommentService commentService;

    @Operation(summary = "Create a new comment on a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid comment data provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<CommentDto> create(
            @Parameter(description = "Task identifier to comment on") @PathVariable UUID taskId,
            @Parameter(description = "Comment content") @Valid @RequestBody CommentRequestDto dto) {
        return ApiResponseDto.buildSuccessResponse(commentService.create(taskId, dto));
    }

    @Operation(
            summary = "Find all comments for a task",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=createdAt,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of comments", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDto.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<CommentDto> findAll(
            @Parameter(description = "Task identifier") @PathVariable UUID taskId,
            @ParameterObject Pageable pageable) {
        return commentService.findAll(taskId, pageable);
    }

    @Operation(summary = "Update an existing comment (author only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommentDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid comment data provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied — not the comment author"),
            @ApiResponse(responseCode = "404", description = "Task or comment not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<CommentDto> update(
            @Parameter(description = "Task identifier") @PathVariable UUID taskId,
            @Parameter(description = "Comment identifier to update") @PathVariable UUID commentId,
            @Parameter(description = "Updated comment content") @Valid @RequestBody CommentRequestDto dto) {
        return ApiResponseDto.buildSuccessResponse(commentService.update(taskId, commentId, dto));
    }

    @Operation(summary = "Delete a comment (author or ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied — not the comment author"),
            @ApiResponse(responseCode = "404", description = "Task or comment not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(
            @Parameter(description = "Task identifier") @PathVariable UUID taskId,
            @Parameter(description = "Comment identifier to delete") @PathVariable UUID commentId) {
        commentService.delete(taskId, commentId);
        return ApiResponseDto.buildSuccessResponse(null);
    }
}
