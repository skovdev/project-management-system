package local.pms.aiservice.controller;

import local.pms.aiservice.config.SecurityConfig;
import local.pms.aiservice.config.jwt.JwtTokenProvider;

import local.pms.aiservice.exception.ChatGptException;

import local.pms.aiservice.service.TokenService;

import local.pms.aiservice.service.chatgpt.ChatGptService;

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

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatGptController.class)
@Import(SecurityConfig.class)
class ChatGptControllerTest {

    private static final String BASE_URL = "/api/v1/chat-gpt";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGptService chatGptService;

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
    @DisplayName("POST /chat-gpt/ask with valid token returns 200 with AI response")
    void should_return200_when_askWithValidToken() throws Exception {
        when(chatGptService.askChatGpt(any())).thenReturn("Generated project description");

        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value("Generated project description"));
    }

    @Test
    @DisplayName("POST /chat-gpt/ask without token returns 401")
    void should_return401_when_askWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL + "/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /chat-gpt/ask when ChatGPT service fails returns 500")
    void should_return500_when_chatGptServiceThrowsException() throws Exception {
        when(chatGptService.askChatGpt(any()))
                .thenThrow(new ChatGptException("Failed to communicate with ChatGPT"));

        mockMvc.perform(post(BASE_URL + "/ask")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isInternalServerError());
    }
}