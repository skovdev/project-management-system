package local.pms.aiservice.controller;

import local.pms.aiservice.config.SecurityConfig;

import local.pms.aiservice.config.jwt.JwtTokenProvider;

import local.pms.aiservice.exception.AiChatException;

import local.pms.aiservice.service.TokenService;
import local.pms.aiservice.service.ProjectAiService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProjectAiRestController.class)
@Import(SecurityConfig.class)
class ProjectAiRestControllerTest {

    private static final String BASE_URL = "/api/v1/ai/project";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectAiService projectAiService;

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
    @DisplayName("POST /ai/project/description with valid input returns 200 with generated description")
    void should_return200_when_validRequest() throws Exception {
        when(projectAiService.generateProjectDescription(any())).thenReturn("A system for managing projects.");

        mockMvc.perform(post(BASE_URL + "/description")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Project Management System"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value("A system for managing projects."));
    }

    @Test
    @DisplayName("POST /ai/project/description with blank title returns 400 VALIDATION_ERROR")
    void should_return400_when_titleIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/description")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ai/project/description without token returns 401")
    void should_return401_when_noToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Project Management System"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /ai/project/description when AI model fails returns 500 AI_SERVICE_ERROR")
    void should_return500_when_aiServiceThrows() throws Exception {
        when(projectAiService.generateProjectDescription(any()))
                .thenThrow(new AiChatException("Failed to communicate with AI model"));

        mockMvc.perform(post(BASE_URL + "/description")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Project Management System"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("AI_SERVICE_ERROR"));
    }
}
