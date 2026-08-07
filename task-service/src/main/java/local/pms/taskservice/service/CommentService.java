package local.pms.taskservice.service;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing comments on tasks.
 */
public interface CommentService {

    /**
     * Creates a new comment on the specified task authored by the authenticated user.
     * The caller must be a member of the task's organization.
     *
     * @param taskId the task to comment on
     * @param dto    the comment content
     * @return the created comment
     */
    CommentDto create(UUID taskId, CommentRequestDto dto);

    /**
     * Returns a paginated list of comments for the specified task.
     * The caller must be a member of the task's organization.
     *
     * @param taskId   the task whose comments to retrieve
     * @param pageable pagination and sorting parameters
     * @return a page of comment DTOs
     */
    Page<CommentDto> findAll(UUID taskId, Pageable pageable);

    /**
     * Updates the content of a comment. The caller must be a member of the task's
     * organization and the original author of the comment.
     *
     * @param taskId    the task that owns the comment
     * @param commentId the comment to update
     * @param dto       the new comment content
     * @return the updated comment
     */
    CommentDto update(UUID taskId, UUID commentId, CommentRequestDto dto);

    /**
     * Deletes a comment. The caller must be a member of the task's organization and
     * the original author of the comment (no organization-role-based bypass).
     *
     * @param taskId    the task that owns the comment
     * @param commentId the comment to delete
     */
    void delete(UUID taskId, UUID commentId);
}
