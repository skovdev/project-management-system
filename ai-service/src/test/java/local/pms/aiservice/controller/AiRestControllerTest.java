package local.pms.aiservice.controller;

import local.pms.aiservice.config.SecurityConfig;

import local.pms.aiservice.config.jwt.JwtTokenProvider;

import local.pms.aiservice.exception.AiChatException;

import local.pms.aiservice.service.AiChatService;
import local.pms.aiservice.service.TokenService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AiRestController.class)
@Import(SecurityConfig.class)
class AiRestControllerTest {

    private static final String BASE_URL = "/api/v1/ai";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenService tokenService;

    @BeforeEach
    void setUpJwtMocksAsUser() {
        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("POST /ai/ask with valid prompts returns 200 with AI response")
    void should_return200_when_askWithValidPrompts() throws Exception {
        when(aiChatService.chat(any(), any())).thenReturn("Generated acceptance criteria");

        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemPrompt": "You are a helpful assistant.",
                                  "userPrompt": "Generate acceptance criteria for this task."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value("Generated acceptance criteria"));
    }

    @Test
    @DisplayName("POST /ai/ask with blank systemPrompt returns 400 with VALIDATION_ERROR code")
    void should_return400_when_systemPromptIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemPrompt": "",
                                  "userPrompt": "Generate acceptance criteria."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ai/ask with blank userPrompt returns 400 with VALIDATION_ERROR code")
    void should_return400_when_userPromptIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemPrompt": "You are a helpful assistant.",
                                  "userPrompt": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ai/ask without token returns 401")
    void should_return401_when_askWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemPrompt": "You are a helpful assistant.",
                                  "userPrompt": "Generate something."
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /ai/ask when AI service fails returns 500 with AI_SERVICE_ERROR code")
    void should_return500_when_aiServiceThrowsException() throws Exception {
        when(aiChatService.chat(any(), any()))
                .thenThrow(new AiChatException("Failed to communicate with AI model"));

        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "systemPrompt": "You are a helpful assistant.",
                                  "userPrompt": "Generate something."
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("AI_SERVICE_ERROR"));
    }
}
