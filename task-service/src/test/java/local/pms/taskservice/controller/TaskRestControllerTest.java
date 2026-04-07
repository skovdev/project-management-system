package local.pms.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.taskservice.config.SecurityConfig;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;

import local.pms.taskservice.service.TaskService;
import local.pms.taskservice.service.TokenService;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

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

@WebMvcTest(TaskRestController.class)
@Import(SecurityConfig.class)
class TaskRestControllerTest {

    private static final String BASE_URL = "/api/v1/tasks";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

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
    @DisplayName("POST /tasks with valid body returns 200")
    void should_return200_when_createWithValidBody() throws Exception {
        var dto = buildTaskDto(null);
        var created = buildTaskDto(UUID.randomUUID().toString());
        when(taskService.create(any(TaskDto.class))).thenReturn(created);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.title").value("My Task"));
    }

    @Test
    @DisplayName("POST /tasks with blank title returns 400")
    void should_return400_when_createWithBlankTitle() throws Exception {
        var body = new TaskDto(null, "", "A task description", TaskStatusType.TODO,
                TaskPriorityType.MEDIUM, true, UUID.randomUUID().toString(), null);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tasks with blank description returns 400")
    void should_return400_when_createWithBlankDescription() throws Exception {
        var body = new TaskDto(null, "My Task", "", TaskStatusType.TODO,
                TaskPriorityType.MEDIUM, true, UUID.randomUUID().toString(), null);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tasks with blank projectId returns 400")
    void should_return400_when_createWithBlankProjectId() throws Exception {
        var body = new TaskDto(null, "My Task", "A task description", TaskStatusType.TODO,
                TaskPriorityType.MEDIUM, true, "", null);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tasks without token returns 401")
    void should_return401_when_createWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTaskDto(null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /tasks with valid token returns 200 with page")
    void should_return200_when_findAllWithValidToken() throws Exception {
        var dto = buildTaskDto(UUID.randomUUID().toString());
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(taskService.findAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("My Task"));
    }

    @Test
    @DisplayName("GET /tasks without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("GET /tasks/{id} with valid token returns 200")
    void should_return200_when_findByIdWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildTaskDto(id.toString());
        when(taskService.findById(id)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /tasks/{id} when task not found returns 404")
    void should_return404_when_findByIdNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(taskService.findById(id))
                .thenThrow(new TaskNotFoundException("Task with ID " + id + " not found"));

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks/{id} without token returns 401")
    void should_return401_when_findByIdWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /tasks/{id} with valid body returns 200")
    void should_return200_when_updateWithValidBody() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildTaskDto(id.toString());
        when(taskService.update(eq(id), any(TaskDto.class))).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("My Task"));
    }

    @Test
    @DisplayName("PUT /tasks/{id} with blank title returns 400")
    void should_return400_when_updateWithBlankTitle() throws Exception {
        var id = UUID.randomUUID();
        var body = new TaskDto(id.toString(), "", "A task description", TaskStatusType.TODO,
                TaskPriorityType.MEDIUM, true, UUID.randomUUID().toString(), null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /tasks/{id} when task not found returns 404")
    void should_return404_when_updateNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(taskService.update(eq(id), any(TaskDto.class)))
                .thenThrow(new TaskNotFoundException("Task with ID " + id + " not found"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTaskDto(id.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /tasks/{id} when access denied returns 403")
    void should_return403_when_updateAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        when(taskService.update(eq(id), any(TaskDto.class)))
                .thenThrow(new TaskAccessDeniedException("Access denied: you do not own task with ID " + id));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTaskDto(id.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /tasks/{id} without token returns 401")
    void should_return401_when_updateWithoutToken() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildTaskDto(id.toString()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} with ADMIN role returns 200")
    void should_return200_when_deleteWithAdminRole() throws Exception {
        var id = UUID.randomUUID();
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        doNothing().when(taskService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} with USER role returns 403")
    void should_return403_when_deleteWithUserRole() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()).header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} when task not found returns 404")
    void should_return404_when_deleteNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        doThrow(new TaskNotFoundException("Task with ID " + id + " not found"))
                .when(taskService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} without token returns 401")
    void should_return401_when_deleteWithoutToken() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private TaskDto buildTaskDto(String id) {
        return new TaskDto(
                id,
                "My Task",
                "A task description",
                TaskStatusType.TODO,
                TaskPriorityType.MEDIUM,
                true,
                UUID.randomUUID().toString(),
                null
        );
    }
}
