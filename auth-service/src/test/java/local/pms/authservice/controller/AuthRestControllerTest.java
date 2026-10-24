package local.pms.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.authservice.config.jwt.JwtTokenProvider;

import local.pms.authservice.dto.authuser.AuthUserDto;

import local.pms.authservice.exception.AuthUserNotFoundException;

import local.pms.authservice.service.AuthService;

import local.pms.authservice.config.security.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(AuthRestController.class)
@Import(SecurityConfig.class)
class AuthRestControllerTest {

    private static final String BASE_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /sign-up with valid body returns 200")
    void should_return200_when_signUpWithValidBody() throws Exception {
        doNothing().when(authService).signUp(any());

        mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "password1", "alice@mail.com", "Alice", "Wonder")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("POST /sign-up with blank username returns 400")
    void should_return400_when_signUpWithBlankUsername() throws Exception {
        mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("", "password1", "alice@mail.com", "Alice", "Wonder")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sign-up with short password returns 400")
    void should_return400_when_signUpWithShortPassword() throws Exception {
        mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "ab", "alice@mail.com", "Alice", "Wonder")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sign-up with invalid email returns 400")
    void should_return400_when_signUpWithInvalidEmail() throws Exception {
        mockMvc.perform(post(BASE_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "password1", "not-an-email", "Alice", "Wonder")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /sign-in with valid credentials returns 200 with token")
    void should_return200_when_signInWithValidCredentials() throws Exception {
        var id = UUID.randomUUID();
        var authUserDto = buildAuthUserDto(id, "alice");
        when(authService.authenticate("alice", "secret")).thenReturn(authUserDto);
        when(authService.generateToken(authUserDto)).thenReturn("jwt.token.here");

        mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    @DisplayName("POST /sign-in with blank username returns 400")
    void should_return400_when_signInWithBlankUsername() throws Exception {
        mockMvc.perform(post(BASE_URL + "/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());
    }

    private String signUpJson(String username, String password, String email, String firstName, String lastName) {
        return String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                username, password, email, firstName, lastName);
    }

    private AuthUserDto buildAuthUserDto(UUID id, String username) {
        return new AuthUserDto(id, username);
    }
}
