package local.pms.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.notificationservice.config.SecurityConfig;

import local.pms.notificationservice.config.jwt.JwtTokenProvider;

import local.pms.notificationservice.dto.NotificationDto;

import local.pms.notificationservice.exception.NotificationNotFoundException;

import local.pms.notificationservice.service.NotificationService;
import local.pms.notificationservice.service.TokenService;

import local.pms.notificationservice.type.NotificationTypeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@WebMvcTest(NotificationRestController.class)
@Import(SecurityConfig.class)
class NotificationRestControllerTest {

    private static final String BASE_URL = "/api/v1/notifications";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenService tokenService;

    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp() {
        userAuth = new UsernamePasswordAuthenticationToken(
                "testuser", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
        when(jwtTokenProvider.extractAuthorities(any()))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /notifications returns 200 with paginated notifications")
    void should_return200_when_findAll() throws Exception {
        var dto = buildNotificationDto(false);
        when(notificationService.findAll(any())).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Welcome to PMS!"));
    }

    @Test
    @DisplayName("GET /notifications/unread returns 200 with unread notifications list")
    void should_return200_when_findUnread() throws Exception {
        var dto = buildNotificationDto(false);
        when(notificationService.findUnread()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL + "/unread")
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].read").value(false));
    }

    @Test
    @DisplayName("GET /notifications/{id} returns 200 when notification found")
    void should_return200_when_findById() throws Exception {
        var dto = buildNotificationDto(false);
        when(notificationService.findById(any())).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID())
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("WELCOME"));
    }

    @Test
    @DisplayName("GET /notifications/{id} returns 404 when notification not found")
    void should_return404_when_notificationNotFound() throws Exception {
        when(notificationService.findById(any())).thenThrow(new NotificationNotFoundException("Not found"));

        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID())
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read returns 200 with updated notification")
    void should_return200_when_markAsRead() throws Exception {
        var dto = buildNotificationDto(true);
        when(notificationService.markAsRead(any())).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID() + "/read")
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    @DisplayName("DELETE /notifications/{id} returns 200 on successful deletion")
    void should_return200_when_delete() throws Exception {
        doNothing().when(notificationService).delete(any());

        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /notifications/{id} returns 404 when notification not found")
    void should_return404_when_deletingNonExistentNotification() throws Exception {
        doThrow(new NotificationNotFoundException("Not found")).when(notificationService).delete(any());

        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())
                        .header("Authorization", BEARER)
                        .with(authentication(userAuth)))
                .andExpect(status().isNotFound());
    }

    private NotificationDto buildNotificationDto(boolean read) {
        return new NotificationDto(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                NotificationTypeType.WELCOME,
                "Welcome to PMS!",
                "Your account was created successfully.",
                read,
                Instant.now()
        );
    }
}
