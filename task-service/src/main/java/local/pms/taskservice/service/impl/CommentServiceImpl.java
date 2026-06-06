package local.pms.taskservice.service.impl;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.entity.Comment;

import local.pms.taskservice.event.CommentCreatedEvent;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.CommentNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.CommentAccessDeniedException;

import local.pms.taskservice.mapping.CommentMapping;

import local.pms.taskservice.repository.TaskRepository;
import local.pms.taskservice.repository.CommentRepository;

import local.pms.taskservice.service.TokenService;
import local.pms.taskservice.service.CommentService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for managing comments on tasks.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final CommentMapping commentMapping;
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentDto create(UUID taskId, CommentRequestDto dto) {
        verifyTaskExists(taskId);
        var authorId = extractAuthUserId();

        var comment = new Comment();
        comment.setContent(dto.content());
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);

        var saved = commentRepository.save(comment);
        log.info("Comment created with ID: {} on taskId: {} by authorId: {}", saved.getId(), taskId, authorId);

        eventPublisher.publishEvent(
                new CommentCreatedEvent(saved.getId(), saved.getTaskId(), saved.getAuthorId(), saved.getContent()));

        return commentMapping.toDto(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> findAll(UUID taskId, Pageable pageable) {
        verifyTaskExists(taskId);
        return commentRepository.findAllByTaskId(taskId, pageable)
                .map(commentMapping::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentDto update(UUID taskId, UUID commentId, CommentRequestDto dto) {
        verifyTaskExists(taskId);
        var authorId = extractAuthUserId();

        var comment = resolveCommentForAuthor(commentId, taskId, authorId);
        comment.setContent(dto.content());
        var updated = commentRepository.save(comment);
        log.info("Comment with ID: {} updated by authorId: {}", commentId, authorId);
        return commentMapping.toDto(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(UUID taskId, UUID commentId) {
        verifyTaskExists(taskId);

        if (isAdmin()) {
            var comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                    .orElseThrow(() -> {
                        log.error("Comment {} not found on task {} for admin delete.", commentId, taskId);
                        return new CommentNotFoundException("Comment with ID " + commentId + " not found on task " + taskId);
                    });
            commentRepository.deleteById(comment.getId());
            log.info("Comment with ID: {} on task {} deleted by ADMIN.", commentId, taskId);
        } else {
            var authorId = extractAuthUserId();
            var comment = resolveCommentForAuthor(commentId, taskId, authorId);
            commentRepository.deleteById(comment.getId());
            log.info("Comment with ID: {} on task {} deleted by authorId: {}", commentId, taskId, authorId);
        }
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

    private void verifyTaskExists(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            log.error("Task with ID {} not found.", taskId);
            throw new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
        }
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract authenticated user ID.");
            throw new TaskAccessDeniedException("Access denied: missing or invalid authentication token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }
}
