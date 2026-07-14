package local.pms.projectservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.projectservice.config.SecurityConfig;

import local.pms.projectservice.config.jwt.JwtTokenProvider;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.exception.ProjectNotFoundException;
import local.pms.projectservice.exception.ProjectAccessDeniedException;
import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.service.TokenService;
import local.pms.projectservice.service.ProjectService;

import local.pms.projectservice.type.ProjectStatusType;

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

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProjectRestController.class)
@Import(SecurityConfig.class)
class ProjectRestControllerTest {

    private static final String BASE_URL = "/api/v1/projects";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

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
    @DisplayName("POST /projects with valid body returns 200")
    void should_return200_when_createWithValidBody() throws Exception {
        var organizationId = UUID.randomUUID();
        var dto = buildProjectDto(null, organizationId);
        var created = buildProjectDto(UUID.randomUUID(), organizationId);
        when(projectService.create(any(ProjectDto.class))).thenReturn(created);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.title").value("My Project"));
    }

    @Test
    @DisplayName("POST /projects with blank title returns 400")
    void should_return400_when_createWithBlankTitle() throws Exception {
        var body = new ProjectDto(null, "", "Description", ProjectStatusType.PLANNING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30), null, UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /projects with missing organizationId returns 400")
    void should_return400_when_createWithMissingOrganizationId() throws Exception {
        var body = new ProjectDto(null, "My Project", "Description", ProjectStatusType.PLANNING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30), null, null);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /projects when caller lacks organization permission returns 403")
    void should_return403_when_createAccessDenied() throws Exception {
        when(projectService.create(any(ProjectDto.class)))
                .thenThrow(new ProjectAccessDeniedException("Access denied"));

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProjectDto(null, UUID.randomUUID()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /projects without token returns 401")
    void should_return401_when_createWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProjectDto(null, UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /projects with valid token and organizationId returns 200 with page")
    void should_return200_when_findAllWithValidToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var dto = buildProjectDto(UUID.randomUUID(), organizationId);
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(projectService.findAll(eq(organizationId), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("My Project"));
    }

    @Test
    @DisplayName("GET /projects without organizationId param returns 400")
    void should_return400_when_findAllWithoutOrganizationId() throws Exception {
        mockMvc.perform(get(BASE_URL).header("Authorization", BEARER))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /projects without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL).param("organizationId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /projects/{id} with valid token returns 200")
    void should_return200_when_findByIdWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var dto = buildProjectDto(id, organizationId);
        when(projectService.findById(id, organizationId)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/" + id).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /projects/{id} when project not found returns 404")
    void should_return404_when_findByIdNotFound() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(projectService.findById(id, organizationId))
                .thenThrow(new ProjectNotFoundException("Project with ID " + id + " not found"));

        mockMvc.perform(get(BASE_URL + "/" + id).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /projects/{id} without token returns 401")
    void should_return401_when_findByIdWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()).param("organizationId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /projects/{id} with valid body returns 200")
    void should_return200_when_updateWithValidBody() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var dto = buildProjectDto(id, organizationId);
        when(projectService.update(eq(id), any(ProjectDto.class))).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("My Project"));
    }

    @Test
    @DisplayName("PUT /projects/{id} with blank title returns 400")
    void should_return400_when_updateWithBlankTitle() throws Exception {
        var id = UUID.randomUUID();
        var body = new ProjectDto(id, "", "Description", ProjectStatusType.PLANNING,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30), null, UUID.randomUUID());

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /projects/{id} when project not found returns 404")
    void should_return404_when_updateNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(projectService.update(eq(id), any(ProjectDto.class)))
                .thenThrow(new ProjectNotFoundException("Project with ID " + id + " not found"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProjectDto(id, UUID.randomUUID()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /projects/{id} when access denied returns 403")
    void should_return403_when_updateAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        when(projectService.update(eq(id), any(ProjectDto.class)))
                .thenThrow(new ProjectAccessDeniedException("Access denied: you do not have sufficient permissions"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProjectDto(id, UUID.randomUUID()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /projects/{id} without token returns 401")
    void should_return401_when_updateWithoutToken() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProjectDto(id, UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /projects/{id} with valid token returns 200")
    void should_return200_when_deleteWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        doNothing().when(projectService).delete(id, organizationId);

        mockMvc.perform(delete(BASE_URL + "/" + id).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /projects/{id} when project not found returns 404")
    void should_return404_when_deleteNotFound() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        doThrow(new ProjectNotFoundException("Project with ID " + id + " not found"))
                .when(projectService).delete(id, organizationId);

        mockMvc.perform(delete(BASE_URL + "/" + id).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /projects/{id} when caller lacks OWNER/ADMIN role returns 403")
    void should_return403_when_deleteAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        doThrow(new ProjectAccessDeniedException("Access denied"))
                .when(projectService).delete(id, organizationId);

        mockMvc.perform(delete(BASE_URL + "/" + id).param("organizationId", organizationId.toString()).header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /projects/{id} without token returns 401")
    void should_return401_when_deleteWithoutToken() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()).param("organizationId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /projects/{id}/description returns 200 with generated text")
    void should_return200_when_generateDescriptionSucceeds() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(projectService.generateProjectDescription(id, organizationId, "My Project"))
                .thenReturn("A cool project description");

        mockMvc.perform(post(BASE_URL + "/" + id + "/description")
                        .header("Authorization", BEARER)
                        .param("organizationId", organizationId.toString())
                        .param("projectTitle", "My Project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("A cool project description"));
    }

    @Test
    @DisplayName("POST /projects/{id}/description when project not found returns 404")
    void should_return404_when_generateDescriptionProjectNotFound() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(projectService.generateProjectDescription(eq(id), eq(organizationId), any()))
                .thenThrow(new ProjectNotFoundException("Project with ID " + id + " not found"));

        mockMvc.perform(post(BASE_URL + "/" + id + "/description")
                        .header("Authorization", BEARER)
                        .param("organizationId", organizationId.toString())
                        .param("projectTitle", "My Project"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /projects/{id}/description when AI fails returns 500")
    void should_return500_when_generateDescriptionAiFails() throws Exception {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(projectService.generateProjectDescription(eq(id), eq(organizationId), any()))
                .thenThrow(new DescriptionGenerationException("AI error", new RuntimeException()));

        mockMvc.perform(post(BASE_URL + "/" + id + "/description")
                        .header("Authorization", BEARER)
                        .param("organizationId", organizationId.toString())
                        .param("projectTitle", "My Project"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /projects/{id}/description without token returns 401")
    void should_return401_when_generateDescriptionWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + UUID.randomUUID() + "/description")
                        .param("organizationId", UUID.randomUUID().toString())
                        .param("projectTitle", "My Project"))
                .andExpect(status().isUnauthorized());
    }

    private ProjectDto buildProjectDto(UUID id, UUID organizationId) {
        return new ProjectDto(
                id,
                "My Project",
                "A project description",
                ProjectStatusType.PLANNING,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 0, 0),
                null,
                organizationId
        );
    }
}
