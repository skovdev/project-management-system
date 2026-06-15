package local.pms.aiservice.controller;

import local.pms.aiservice.config.SecurityConfig;

import local.pms.aiservice.config.jwt.JwtTokenProvider;

import local.pms.aiservice.exception.AiChatException;

import local.pms.aiservice.service.TokenService;
import local.pms.aiservice.service.TaskAiService;

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

@WebMvcTest(TaskAiRestController.class)
@Import(SecurityConfig.class)
class TaskAiRestControllerTest {

    private static final String BASE_URL = "/api/v1/ai/task";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskAiService taskAiService;

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
    @DisplayName("POST /ai/task/acceptance-criteria with valid input returns 200 with generated criteria")
    void should_return200_when_validRequest() throws Exception {
        when(taskAiService.generateAcceptanceCriteria(any(), any())).thenReturn("Given ... When ... Then ...");

        mockMvc.perform(post(BASE_URL + "/acceptance-criteria")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement user login",
                                  "description": "Allow users to log in using email and password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value("Given ... When ... Then ..."));
    }

    @Test
    @DisplayName("POST /ai/task/acceptance-criteria with blank title returns 400 VALIDATION_ERROR")
    void should_return400_when_titleIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/acceptance-criteria")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Allow users to log in using email and password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ai/task/acceptance-criteria with blank description returns 400 VALIDATION_ERROR")
    void should_return400_when_descriptionIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/acceptance-criteria")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement user login",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ai/task/acceptance-criteria without token returns 401")
    void should_return401_when_noToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/acceptance-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement user login",
                                  "description": "Allow users to log in using email and password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /ai/task/acceptance-criteria when AI model fails returns 500 AI_SERVICE_ERROR")
    void should_return500_when_aiServiceThrows() throws Exception {
        when(taskAiService.generateAcceptanceCriteria(any(), any()))
                .thenThrow(new AiChatException("Failed to communicate with AI model"));

        mockMvc.perform(post(BASE_URL + "/acceptance-criteria")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement user login",
                                  "description": "Allow users to log in using email and password"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("AI_SERVICE_ERROR"));
    }
}
