package local.pms.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.userservice.config.SecurityConfig;

import local.pms.userservice.config.jwt.JwtTokenProvider;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import local.pms.userservice.service.UserService;
import local.pms.userservice.service.TokenService;

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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserRestController.class)
@Import(SecurityConfig.class)
class UserRestControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setUpJwtMocks() {
        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /users with valid token returns 200 with page")
    void should_return200_when_findAllWithValidToken() throws Exception {
        var dto = buildUserDto(UUID.randomUUID(), UUID.randomUUID());
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(userService.findAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    @Test
    @DisplayName("GET /users without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/{id} with valid token returns 200")
    void should_return200_when_findByIdWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildUserDto(id, UUID.randomUUID());
        when(userService.findById(id)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /users/{id} when user not found returns 404")
    void should_return404_when_findByIdNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(userService.findById(id))
                .thenThrow(new UserNotFoundException("User with id '" + id + "' not found"));

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/{id} without token returns 401")
    void should_return401_when_findByIdWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /users/{id} with valid body returns 200")
    void should_return200_when_updateWithValidBody() throws Exception {
        var id = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var dto = buildUserDto(id, authUserId);
        when(userService.update(eq(id), any(UserDto.class))).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @DisplayName("PUT /users/{id} with blank firstName returns 400")
    void should_return400_when_updateWithBlankFirstName() throws Exception {
        var id = UUID.randomUUID();
        var body = new UserDto(id, "", "Smith", "alice@mail.com", UUID.randomUUID());

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /users/{id} with invalid email returns 400")
    void should_return400_when_updateWithInvalidEmail() throws Exception {
        var id = UUID.randomUUID();
        var body = new UserDto(id, "Alice", "Smith", "not-an-email", UUID.randomUUID());

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /users/{id} when user not found returns 404")
    void should_return404_when_updateNotFound() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildUserDto(id, UUID.randomUUID());
        when(userService.update(eq(id), any(UserDto.class)))
                .thenThrow(new UserNotFoundException("User with id '" + id + "' not found"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /users/{id} when access denied returns 403")
    void should_return403_when_updateAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildUserDto(id, UUID.randomUUID());
        when(userService.update(eq(id), any(UserDto.class)))
                .thenThrow(new UserAccessDeniedException("Access denied to user with id '" + id + "'"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /users/{id} without token returns 401")
    void should_return401_when_updateWithoutToken() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildUserDto(id, UUID.randomUUID());

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /users/{id} with valid token returns 204")
    void should_return204_when_deleteWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(userService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /users/{id} when user not found returns 404")
    void should_return404_when_deleteNotFound() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new UserNotFoundException("User with id '" + id + "' not found"))
                .when(userService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /users/{id} when access denied returns 403")
    void should_return403_when_deleteAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new UserAccessDeniedException("Access denied to user with id '" + id + "'"))
                .when(userService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /users/{id} without token returns 401")
    void should_return401_when_deleteWithoutToken() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private UserDto buildUserDto(UUID id, UUID authUserId) {
        return new UserDto(id, "Alice", "Smith", "alice@mail.com", authUserId);
    }
}
