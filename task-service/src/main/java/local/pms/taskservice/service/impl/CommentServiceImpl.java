package local.pms.taskservice.service.impl;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.entity.Task;
import local.pms.taskservice.entity.Comment;

import local.pms.taskservice.event.CommentCreatedEvent;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.CommentNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.CommentAccessDeniedException;
import local.pms.taskservice.exception.CommentSuggestionGenerationException;

import local.pms.taskservice.external.ai.provider.AiExternalProvider;

import local.pms.taskservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.taskservice.mapping.CommentMapping;

import local.pms.taskservice.repository.TaskRepository;
import local.pms.taskservice.repository.CommentRepository;

import local.pms.taskservice.service.TokenService;
import local.pms.taskservice.service.CommentService;

import local.pms.taskservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.UUID;

import java.util.stream.Collectors;

/**
 * Service implementation for managing comments on tasks.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private static final int COMMENT_THREAD_CONTEXT_LIMIT = 10;

    private final CommentMapping commentMapping;
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final AiExternalProvider aiExternalProvider;
    private final OrganizationAccessProvider organizationAccessProvider;
    private final TaskOrganizationResolver taskOrganizationResolver;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentDto create(UUID taskId, CommentRequestDto dto) {
        verifyTaskAccess(taskId);
        var authorId = extractAuthUserId();

        var comment = new Comment();
        comment.setContent(dto.content());
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);
        comment.setParentCommentId(resolveParentCommentId(taskId, dto.parentCommentId()));

        var saved = commentRepository.save(comment);
        log.info("Comment created with ID: {} on taskId: {} by authorId: {}", saved.getId(), taskId, authorId);

        eventPublisher.publishEvent(
                new CommentCreatedEvent(saved.getId(), saved.getTaskId(), saved.getAuthorId(), saved.getContent()));

        return commentMapping.toDto(saved).withReplies(List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> findAll(UUID taskId, Pageable pageable) {
        verifyTaskAccess(taskId);
        var topLevelPage = commentRepository.findAllByTaskIdAndParentCommentIdIsNull(taskId, pageable);

        var topLevelIds = topLevelPage.getContent().stream().map(Comment::getId).toList();
        var repliesByParentId = topLevelIds.isEmpty()
                ? Map.<UUID, List<Comment>>of()
                : commentRepository.findAllByParentCommentIdInOrderByCreatedAtAsc(topLevelIds).stream()
                        .collect(Collectors.groupingBy(Comment::getParentCommentId));

        return topLevelPage.map(comment -> {
            var replies = repliesByParentId.getOrDefault(comment.getId(), List.of()).stream()
                    .map(reply -> commentMapping.toDto(reply).withReplies(List.of()))
                    .toList();
            return commentMapping.toDto(comment).withReplies(replies);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentDto update(UUID taskId, UUID commentId, CommentRequestDto dto) {
        verifyTaskAccess(taskId);
        var authorId = extractAuthUserId();

        var comment = resolveCommentForAuthor(commentId, taskId, authorId);
        comment.setContent(dto.content());
        var updated = commentRepository.save(comment);
        log.info("Comment with ID: {} updated by authorId: {}", commentId, authorId);
        return commentMapping.toDto(updated).withReplies(List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(UUID taskId, UUID commentId) {
        var role = verifyTaskAccess(taskId);
        var authorId = extractAuthUserId();

        if (role.isAtLeast(OrganizationRoleType.ADMIN)) {
            var comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                    .orElseThrow(() -> {
                        log.error("Comment {} not found on task {}.", commentId, taskId);
                        return new CommentNotFoundException("Comment with ID " + commentId + " not found on task " + taskId);
                    });
            commentRepository.deleteById(comment.getId());
            log.info("Comment with ID: {} on task {} deleted by organization {} authorId: {}", commentId, taskId, role, authorId);
            return;
        }

        var comment = resolveCommentForAuthor(commentId, taskId, authorId);
        commentRepository.deleteById(comment.getId());
        log.info("Comment with ID: {} on task {} deleted by authorId: {}", commentId, taskId, authorId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> generateReplySuggestions(UUID taskId, UUID commentId) {
        var task = findTaskOrThrow(taskId);
        organizationAccessProvider.verifyMembership(taskOrganizationResolver.resolve(task));

        var comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                .orElseThrow(() -> {
                    log.error("Comment {} not found on task {}.", commentId, taskId);
                    return new CommentNotFoundException("Comment with ID " + commentId + " not found on task " + taskId);
                });

        var threadContext = getCommentThreadContext(taskId, commentId);

        try {
            log.info("Generating reply suggestions for commentId: {} on taskId: {}", commentId, taskId);
            return aiExternalProvider.generateCommentSuggestions(task.getTitle(), task.getDescription(), comment.getContent(), threadContext);
        } catch (Exception e) {
            log.error("Failed to generate reply suggestions for commentId '{}': {}", commentId, e.getMessage());
            throw new CommentSuggestionGenerationException(
                    "An error occurred while generating comment reply suggestions", e);
        }
    }

    private List<String> getCommentThreadContext(UUID taskId, UUID commentId) {
        return commentRepository
                .findAllByTaskId(taskId, PageRequest.of(0, COMMENT_THREAD_CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .filter(c -> !c.getId().equals(commentId))
                .map(Comment::getContent)
                .toList();
    }

    /**
     * Resolves the actual parent to attach a new comment to, flattening replies-to-replies
     * to their thread root so nesting never exceeds one level.
     *
     * @param taskId           the task the comment is being posted to
     * @param requestedParentId the parent comment id from the request, or null for a top-level comment
     * @return the resolved parent comment id, or null for a top-level comment
     */
    private UUID resolveParentCommentId(UUID taskId, UUID requestedParentId) {
        if (requestedParentId == null) {
            return null;
        }
        var parent = commentRepository.findByIdAndTaskId(requestedParentId, taskId)
                .orElseThrow(() -> {
                    log.error("Parent comment {} not found on task {}.", requestedParentId, taskId);
                    return new CommentNotFoundException("Comment with ID " + requestedParentId + " not found on task " + taskId);
                });
        return parent.getParentCommentId() != null ? parent.getParentCommentId() : parent.getId();
    }

    private Comment resolveCommentForAuthor(UUID commentId, UUID taskId, UUID authorId) {
        return commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, authorId)
                .orElseThrow(() -> {
                    log.error("Comment {} not found on task {} or user {} is not the author.", commentId, taskId, authorId);
                    return commentRepository.findByIdAndTaskId(commentId, taskId).isPresent()
                            ? new CommentAccessDeniedException("Access denied: you are not the author of comment with ID " + commentId)
                            : new CommentNotFoundException("Comment with ID " + commentId + " not found on task " + taskId);
                });
    }

    private OrganizationRoleType verifyTaskAccess(UUID taskId) {
        var task = findTaskOrThrow(taskId);
        return organizationAccessProvider.verifyMembership(taskOrganizationResolver.resolve(task));
    }

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task with ID {} not found.", taskId);
                    return new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
                });
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract authenticated user ID.");
            throw new TaskAccessDeniedException("Access denied: missing or invalid authentication token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}
