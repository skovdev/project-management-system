package local.pms.taskservice.service;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.entity.Task;
import local.pms.taskservice.entity.Comment;

import local.pms.taskservice.dto.CommentDto;

import local.pms.taskservice.event.CommentCreatedEvent;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.CommentNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.CommentAccessDeniedException;

import local.pms.taskservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.taskservice.mapping.CommentMapping;

import local.pms.taskservice.repository.CommentRepository;
import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.impl.CommentServiceImpl;
import local.pms.taskservice.service.impl.TaskOrganizationResolver;

import local.pms.taskservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    private static final String COMMENT_CONTENT = "This is a comment.";

    @Mock
    private CommentMapping commentMapping;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrganizationAccessProvider organizationAccessProvider;

    @Mock
    private TaskOrganizationResolver taskOrganizationResolver;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setUpSecurityContextAsUser() {
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(taskOrganizationResolver.resolve(any(Task.class)))
                .thenAnswer(invocation -> ((Task) invocation.getArgument(0)).getOrganizationId());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create saves comment and publishes CommentCreatedEvent after commit")
    void should_saveAndPublishEvent_when_createWithValidData() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var dto = new CommentRequestDto(COMMENT_CONTENT);
        var saved = buildComment(commentId, taskId, authorId);
        var expectedDto = buildCommentDto(commentId, taskId, authorId);

        stubToken(authorId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentMapping.toDto(saved)).thenReturn(expectedDto);

        var result = commentService.create(taskId, dto);

        assertThat(result.id()).isEqualTo(commentId.toString());
        assertThat(result.content()).isEqualTo(COMMENT_CONTENT);
        assertThat(result.taskId()).isEqualTo(taskId.toString());
        assertThat(result.authorId()).isEqualTo(authorId.toString());
        verify(commentRepository).save(any(Comment.class));
        verify(eventPublisher).publishEvent(any(CommentCreatedEvent.class));
    }

    @Test
    @DisplayName("create throws TaskNotFoundException when task does not exist")
    void should_throwTaskNotFoundException_when_taskNotFound_onCreate() {
        var taskId = UUID.randomUUID();
        stubTaskNotFound(taskId);

        assertThatThrownBy(() -> commentService.create(taskId, new CommentRequestDto("text")))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create throws TaskAccessDeniedException when caller is not a member of the task's organization")
    void should_throwTaskAccessDeniedException_when_notOrganizationMember_onCreate() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new TaskAccessDeniedException("Access denied: you are not a member of organization with ID " + organizationId));

        assertThatThrownBy(() -> commentService.create(taskId, new CommentRequestDto("text")))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(organizationId.toString());

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create throws TaskAccessDeniedException when token is missing")
    void should_throwTaskAccessDeniedException_when_tokenMissing_onCreate() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        stubTaskAccess(taskId, organizationId);
        when(tokenService.getToken()).thenReturn(null);

        assertThatThrownBy(() -> commentService.create(taskId, new CommentRequestDto("text")))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining("missing or invalid authentication token");
    }

    @Test
    @DisplayName("create throws TaskAccessDeniedException when token is blank")
    void should_throwTaskAccessDeniedException_when_tokenBlank_onCreate() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        stubTaskAccess(taskId, organizationId);
        when(tokenService.getToken()).thenReturn("   ");

        assertThatThrownBy(() -> commentService.create(taskId, new CommentRequestDto("text")))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining("missing or invalid authentication token");
    }

    @Test
    @DisplayName("findAll returns page of comments for the task")
    void should_returnPageOfComments_when_findAll() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var comment = buildComment(commentId, taskId, authorId);
        var page = new PageImpl<>(List.of(comment), pageable, 1);
        var expectedDto = buildCommentDto(commentId, taskId, authorId);

        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findAllByTaskId(taskId, pageable)).thenReturn(page);
        when(commentMapping.toDto(comment)).thenReturn(expectedDto);

        var result = commentService.findAll(taskId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).content()).isEqualTo(COMMENT_CONTENT);
        assertThat(result.getContent().get(0).taskId()).isEqualTo(taskId.toString());
    }

    @Test
    @DisplayName("findAll returns empty page when no comments exist for the task")
    void should_returnEmptyPage_when_noCommentsExist_onFindAll() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<Comment>(List.of(), pageable, 0);

        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findAllByTaskId(taskId, pageable)).thenReturn(emptyPage);

        var result = commentService.findAll(taskId, pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findAll throws TaskNotFoundException when task does not exist")
    void should_throwTaskNotFoundException_when_taskNotFound_onFindAll() {
        var taskId = UUID.randomUUID();
        stubTaskNotFound(taskId);

        assertThatThrownBy(() -> commentService.findAll(taskId, PageRequest.of(0, 10)))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());
    }

    @Test
    @DisplayName("findAll throws TaskAccessDeniedException when caller is not a member of the task's organization")
    void should_throwTaskAccessDeniedException_when_notOrganizationMember_onFindAll() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new TaskAccessDeniedException("Access denied: you are not a member of organization with ID " + organizationId));

        assertThatThrownBy(() -> commentService.findAll(taskId, PageRequest.of(0, 10)))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(organizationId.toString());
    }

    @Test
    @DisplayName("update returns updated DTO when caller is the author")
    void should_returnUpdatedDto_when_callerIsAuthor() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var existing = buildComment(commentId, taskId, authorId);
        var dto = new CommentRequestDto("Updated content.");
        var expectedDto = buildCommentDto(commentId, taskId, authorId);

        stubToken(authorId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, authorId)).thenReturn(Optional.of(existing));
        when(commentRepository.save(existing)).thenReturn(existing);
        when(commentMapping.toDto(existing)).thenReturn(expectedDto);

        var result = commentService.update(taskId, commentId, dto);

        assertThat(result.id()).isEqualTo(commentId.toString());
        assertThat(result.taskId()).isEqualTo(taskId.toString());
        assertThat(result.authorId()).isEqualTo(authorId.toString());
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("update throws CommentAccessDeniedException when caller is not the author")
    void should_throwCommentAccessDeniedException_when_callerNotAuthor() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var callerId = UUID.randomUUID();

        stubToken(callerId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, callerId)).thenReturn(Optional.empty());
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.of(buildComment(commentId, taskId, UUID.randomUUID())));

        assertThatThrownBy(() -> commentService.update(taskId, commentId, new CommentRequestDto("text")))
                .isInstanceOf(CommentAccessDeniedException.class)
                .hasMessageContaining(commentId.toString());

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws CommentNotFoundException when comment does not exist")
    void should_throwCommentNotFoundException_when_commentNotFound_onUpdate() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var authorId = UUID.randomUUID();

        stubToken(authorId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, authorId)).thenReturn(Optional.empty());
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(taskId, commentId, new CommentRequestDto("text")))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining(commentId.toString());
    }

    @Test
    @DisplayName("update throws TaskNotFoundException when task does not exist")
    void should_throwTaskNotFoundException_when_taskNotFound_onUpdate() {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        stubTaskNotFound(taskId);

        assertThatThrownBy(() -> commentService.update(taskId, commentId, new CommentRequestDto("text")))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws TaskAccessDeniedException when caller is not a member of the task's organization")
    void should_throwTaskAccessDeniedException_when_notOrganizationMember_onUpdate() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new TaskAccessDeniedException("Access denied: you are not a member of organization with ID " + organizationId));

        assertThatThrownBy(() -> commentService.update(taskId, commentId, new CommentRequestDto("text")))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(organizationId.toString());

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes comment when caller is the author (USER role)")
    void should_deleteComment_when_callerIsAuthor() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var existing = buildComment(commentId, taskId, authorId);

        stubToken(authorId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, authorId)).thenReturn(Optional.of(existing));

        commentService.delete(taskId, commentId);

        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("delete removes any comment when caller is an OWNER/ADMIN of the task's organization, even if not the author")
    void should_deleteComment_when_callerIsOrgManager() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var existing = buildComment(commentId, taskId, authorId);

        stubToken(callerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.ADMIN);
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.of(existing));

        commentService.delete(taskId, commentId);

        verify(commentRepository).deleteById(commentId);
        verify(commentRepository, never()).findByIdAndTaskIdAndAuthorId(any(), any(), any());
    }

    @Test
    @DisplayName("delete throws CommentAccessDeniedException when caller has ADMIN role but is not the author (no global admin bypass)")
    void should_throwCommentAccessDeniedException_when_adminRoleButNotAuthor_onDelete() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var callerId = UUID.randomUUID();

        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        stubToken(callerId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, callerId)).thenReturn(Optional.empty());
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.of(buildComment(commentId, taskId, UUID.randomUUID())));

        assertThatThrownBy(() -> commentService.delete(taskId, commentId))
                .isInstanceOf(CommentAccessDeniedException.class)
                .hasMessageContaining(commentId.toString());

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete throws CommentAccessDeniedException when USER is not the author")
    void should_throwCommentAccessDeniedException_when_userNotAuthor_onDelete() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var callerId = UUID.randomUUID();

        stubToken(callerId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, callerId)).thenReturn(Optional.empty());
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.of(buildComment(commentId, taskId, UUID.randomUUID())));

        assertThatThrownBy(() -> commentService.delete(taskId, commentId))
                .isInstanceOf(CommentAccessDeniedException.class)
                .hasMessageContaining(commentId.toString());

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete throws TaskNotFoundException when task does not exist")
    void should_throwTaskNotFoundException_when_taskNotFound_onDelete() {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        stubTaskNotFound(taskId);

        assertThatThrownBy(() -> commentService.delete(taskId, commentId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete throws TaskAccessDeniedException when caller is not a member of the task's organization")
    void should_throwTaskAccessDeniedException_when_notOrganizationMember_onDelete() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new TaskAccessDeniedException("Access denied: you are not a member of organization with ID " + organizationId));

        assertThatThrownBy(() -> commentService.delete(taskId, commentId))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(organizationId.toString());

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete throws CommentNotFoundException when comment does not exist")
    void should_throwCommentNotFoundException_when_commentNotFound_onDelete() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var callerId = UUID.randomUUID();

        stubToken(callerId);
        stubTaskAccess(taskId, organizationId);
        when(commentRepository.findByIdAndTaskIdAndAuthorId(commentId, taskId, callerId)).thenReturn(Optional.empty());
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(taskId, commentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining(commentId.toString());
    }

    private void stubToken(UUID userId) {
        when(tokenService.getToken()).thenReturn("test-token");
        when(jwtTokenProvider.extractAuthUserId("test-token")).thenReturn(userId);
    }

    private void stubTaskAccess(UUID taskId, UUID organizationId) {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(buildTask(taskId, organizationId)));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
    }

    private void stubTaskNotFound(UUID taskId) {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
    }

    private Task buildTask(UUID id, UUID organizationId) {
        var task = new Task();
        task.setId(id);
        task.setOrganizationId(organizationId);
        return task;
    }

    private CommentDto buildCommentDto(UUID commentId, UUID taskId, UUID authorId) {
        return new CommentDto(
                commentId.toString(),
                COMMENT_CONTENT,
                taskId.toString(),
                authorId.toString(),
                Instant.now(),
                Instant.now()
        );
    }

    private Comment buildComment(UUID id, UUID taskId, UUID authorId) {
        var comment = new Comment();
        comment.setId(id);
        comment.setContent(COMMENT_CONTENT);
        comment.setTaskId(taskId);
        comment.setAuthorId(authorId);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        comment.setDeleted(false);
        return comment;
    }
}
