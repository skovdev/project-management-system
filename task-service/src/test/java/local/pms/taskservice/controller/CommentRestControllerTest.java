package local.pms.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.taskservice.config.SecurityConfig;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.CommentDto;
import local.pms.taskservice.dto.CommentRequestDto;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.CommentNotFoundException;
import local.pms.taskservice.exception.CommentAccessDeniedException;

import local.pms.taskservice.service.TokenService;
import local.pms.taskservice.service.CommentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setUpJwtMocksAsUser() {
        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
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

    private CommentDto buildCommentDto(UUID commentId, UUID taskId) {
        return new CommentDto(
                commentId.toString(),
                "This is a comment.",
                taskId.toString(),
                UUID.randomUUID().toString(),
                Instant.now(),
                Instant.now()
        );
    }
}
