package local.pms.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.taskservice.config.SecurityConfig;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.CommentNotFoundException;
import local.pms.taskservice.exception.CommentAccessDeniedException;
import local.pms.taskservice.exception.CommentSuggestionGenerationException;

import local.pms.taskservice.service.TokenService;
import local.pms.taskservice.service.CommentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CommentRestController.class)
@Import(SecurityConfig.class)
class CommentRestControllerTest {

    private static final String BASE_URL = "/api/v1/tasks/{taskId}/comments";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenService tokenService;

    @BeforeEach
    void setUpJwtMocksAsUser() {
        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
    }

    @Test
    @DisplayName("POST /{taskId}/comments with valid body returns 200")
    void should_return200_when_createWithValidBody() throws Exception {
        var taskId = UUID.randomUUID();
        var dto = new CommentRequestDto("This is a comment.");
        var created = buildCommentDto(UUID.randomUUID(), taskId);

        when(commentService.create(eq(taskId), any(CommentRequestDto.class))).thenReturn(created);

        mockMvc.perform(post(BASE_URL, taskId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").value("This is a comment."));
    }

    @Test
    @DisplayName("POST /{taskId}/comments with a parentCommentId returns 200 with the reply attached to that parent")
    void should_return200_when_createReplyWithParentCommentId() throws Exception {
        var taskId = UUID.randomUUID();
        var parentId = UUID.randomUUID();
        var dto = new CommentRequestDto("This is a reply.", parentId);
        var created = buildReplyCommentDto(UUID.randomUUID(), taskId, parentId);

        when(commentService.create(eq(taskId), any(CommentRequestDto.class))).thenReturn(created);

        mockMvc.perform(post(BASE_URL, taskId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("This is a reply."))
                .andExpect(jsonPath("$.data.parentCommentId").value(parentId.toString()))
                .andExpect(jsonPath("$.data.replies").isEmpty());
    }

    @Test
    @DisplayName("POST /{taskId}/comments with blank content returns 400")
    void should_return400_when_createWithBlankContent() throws Exception {
        var taskId = UUID.randomUUID();
        var dto = new CommentRequestDto("");

        mockMvc.perform(post(BASE_URL, taskId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /{taskId}/comments when task not found returns 404")
    void should_return404_when_createTaskNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        when(commentService.create(eq(taskId), any()))
                .thenThrow(new TaskNotFoundException("Task with ID " + taskId + " not found."));

        mockMvc.perform(post(BASE_URL, taskId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequestDto("text"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{taskId}/comments without token returns 401")
    void should_return401_when_createWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequestDto("text"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /{taskId}/comments returns 200 with page")
    void should_return200_when_findAll() throws Exception {
        var taskId = UUID.randomUUID();
        var comment = buildCommentDto(UUID.randomUUID(), taskId);
        var page = new PageImpl<>(List.of(comment), PageRequest.of(0, 10), 1);

        when(commentService.findAll(eq(taskId), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL, taskId).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("This is a comment."));
    }

    @Test
    @DisplayName("GET /{taskId}/comments returns 200 with nested replies on the top-level comment")
    void should_return200_when_findAllWithReplies() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var replyId = UUID.randomUUID();
        var reply = buildReplyCommentDto(replyId, taskId, commentId);
        var comment = buildCommentDto(commentId, taskId).withReplies(List.of(reply));
        var page = new PageImpl<>(List.of(comment), PageRequest.of(0, 10), 1);

        when(commentService.findAll(eq(taskId), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL, taskId).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].replies.length()").value(1))
                .andExpect(jsonPath("$.content[0].replies[0].id").value(replyId.toString()))
                .andExpect(jsonPath("$.content[0].replies[0].parentCommentId").value(commentId.toString()));
    }

    @Test
    @DisplayName("GET /{taskId}/comments without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL, UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /{taskId}/comments/{commentId} with valid body returns 200")
    void should_return200_when_updateWithValidBody() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var dto = new CommentRequestDto("Updated content.");
        var updated = buildCommentDto(commentId, taskId);

        when(commentService.update(eq(taskId), eq(commentId), any(CommentRequestDto.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("This is a comment."));
    }

    @Test
    @DisplayName("PUT /{taskId}/comments/{commentId} when not author returns 403")
    void should_return403_when_updateNotAuthor() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        when(commentService.update(eq(taskId), eq(commentId), any()))
                .thenThrow(new CommentAccessDeniedException("Access denied: you are not the author of comment with ID " + commentId));

        mockMvc.perform(put(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequestDto("text"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /{taskId}/comments/{commentId} when comment not found returns 404")
    void should_return404_when_updateCommentNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        when(commentService.update(eq(taskId), eq(commentId), any()))
                .thenThrow(new CommentNotFoundException("Comment with ID " + commentId + " not found."));

        mockMvc.perform(put(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequestDto("text"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{taskId}/comments/{commentId} without token returns 401")
    void should_return401_when_updateWithoutToken() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{commentId}", UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequestDto("text"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /{taskId}/comments/{commentId} returns 200")
    void should_return200_when_delete() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        doNothing().when(commentService).delete(taskId, commentId);

        mockMvc.perform(delete(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /{taskId}/comments/{commentId} when not author returns 403")
    void should_return403_when_deleteNotAuthor() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        doThrow(new CommentAccessDeniedException("Access denied: you are not the author of comment with ID " + commentId))
                .when(commentService).delete(taskId, commentId);

        mockMvc.perform(delete(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /{taskId}/comments/{commentId} when comment not found returns 404")
    void should_return404_when_deleteCommentNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        doThrow(new CommentNotFoundException("Comment with ID " + commentId + " not found."))
                .when(commentService).delete(taskId, commentId);

        mockMvc.perform(delete(BASE_URL + "/{commentId}", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /{taskId}/comments/{commentId} without token returns 401")
    void should_return401_when_deleteWithoutToken() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{commentId}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /{taskId}/comments/{commentId}/suggestions returns 200 with 3 suggestions")
    void should_return200_when_generateReplySuggestions() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();
        var suggestions = List.of("Suggestion one.", "Suggestion two.", "Suggestion three.");

        when(commentService.generateReplySuggestions(taskId, commentId)).thenReturn(suggestions);

        mockMvc.perform(post(BASE_URL + "/{commentId}/suggestions", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("Suggestion one."));
    }

    @Test
    @DisplayName("POST /{taskId}/comments/{commentId}/suggestions when comment not found returns 404")
    void should_return404_when_generateReplySuggestionsCommentNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        when(commentService.generateReplySuggestions(taskId, commentId))
                .thenThrow(new CommentNotFoundException("Comment with ID " + commentId + " not found."));

        mockMvc.perform(post(BASE_URL + "/{commentId}/suggestions", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{taskId}/comments/{commentId}/suggestions when task not found returns 404")
    void should_return404_when_generateReplySuggestionsTaskNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        when(commentService.generateReplySuggestions(taskId, commentId))
                .thenThrow(new TaskNotFoundException("Task with ID " + taskId + " not found."));

        mockMvc.perform(post(BASE_URL + "/{commentId}/suggestions", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{taskId}/comments/{commentId}/suggestions when AI generation fails returns 500")
    void should_return500_when_generateReplySuggestionsAiFails() throws Exception {
        var taskId = UUID.randomUUID();
        var commentId = UUID.randomUUID();

        when(commentService.generateReplySuggestions(taskId, commentId))
                .thenThrow(new CommentSuggestionGenerationException("An error occurred while generating comment reply suggestions"));

        mockMvc.perform(post(BASE_URL + "/{commentId}/suggestions", taskId, commentId)
                        .header("Authorization", BEARER))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors[0]").value("COMMENT_SUGGESTION_GENERATION_FAILED"));
    }

    @Test
    @DisplayName("POST /{taskId}/comments/{commentId}/suggestions without token returns 401")
    void should_return401_when_generateReplySuggestionsWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{commentId}/suggestions", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private CommentDto buildCommentDto(UUID commentId, UUID taskId) {
        return new CommentDto(
                commentId.toString(),
                "This is a comment.",
                taskId.toString(),
                UUID.randomUUID().toString(),
                null,
                Instant.now(),
                Instant.now(),
                List.of()
        );
    }

    private CommentDto buildReplyCommentDto(UUID commentId, UUID taskId, UUID parentId) {
        return new CommentDto(
                commentId.toString(),
                "This is a reply.",
                taskId.toString(),
                UUID.randomUUID().toString(),
                parentId.toString(),
                Instant.now(),
                Instant.now(),
                List.of()
        );
    }
}
