package local.pms.organizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.organizationservice.config.SecurityConfig;

import local.pms.organizationservice.config.jwt.JwtTokenProvider;

import local.pms.organizationservice.dto.OrganizationDto;

import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;

import local.pms.organizationservice.service.OrganizationService;
import local.pms.organizationservice.service.TokenService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(OrganizationRestController.class)
@Import(SecurityConfig.class)
class OrganizationRestControllerTest {

    private static final String BASE_URL = "/api/v1/organizations";
    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenService tokenService;

    @BeforeEach
    void setUpJwtMocksAsUser() {
        when(jwtTokenProvider.isTokenExpired(any())).thenReturn(false);
        when(jwtTokenProvider.extractUsername(any())).thenReturn("testuser");
    }

    @Test
    @DisplayName("POST /organizations with valid body returns 200")
    void should_return200_when_createWithValidBody() throws Exception {
        var dto = buildOrganizationDto(null, null);
        var created = buildOrganizationDto(UUID.randomUUID(), UUID.randomUUID());
        when(organizationService.create(any(OrganizationDto.class))).thenReturn(created);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("My Organization"));
    }

    @Test
    @DisplayName("POST /organizations with blank name returns 400")
    void should_return400_when_createWithBlankName() throws Exception {
        var body = new OrganizationDto(null, "", "Description", null);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /organizations without token returns 401")
    void should_return401_when_createWithoutToken() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrganizationDto(null, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /organizations with valid token returns 200 with page")
    void should_return200_when_findAllWithValidToken() throws Exception {
        var dto = buildOrganizationDto(UUID.randomUUID(), UUID.randomUUID());
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(organizationService.findAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("My Organization"));
    }

    @Test
    @DisplayName("GET /organizations without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /organizations/{id} with valid token returns 200")
    void should_return200_when_findByIdWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildOrganizationDto(id, UUID.randomUUID());
        when(organizationService.findById(id)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /organizations/{id} when not found returns 404")
    void should_return404_when_findByIdNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(organizationService.findById(id))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + id + " not found"));

        mockMvc.perform(get(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /organizations/{id} without token returns 401")
    void should_return401_when_findByIdWithoutToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /organizations/{id} with valid body returns 200")
    void should_return200_when_updateWithValidBody() throws Exception {
        var id = UUID.randomUUID();
        var dto = buildOrganizationDto(id, UUID.randomUUID());
        when(organizationService.update(eq(id), any(OrganizationDto.class))).thenReturn(dto);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Organization"));
    }

    @Test
    @DisplayName("PUT /organizations/{id} with blank name returns 400")
    void should_return400_when_updateWithBlankName() throws Exception {
        var id = UUID.randomUUID();
        var body = new OrganizationDto(id, "", "Description", null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /organizations/{id} when not found returns 404")
    void should_return404_when_updateNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(organizationService.update(eq(id), any(OrganizationDto.class)))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + id + " not found"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrganizationDto(id, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /organizations/{id} when access denied returns 403")
    void should_return403_when_updateAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        when(organizationService.update(eq(id), any(OrganizationDto.class)))
                .thenThrow(new OrganizationAccessDeniedException("Access denied: insufficient permissions in organization " + id));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrganizationDto(id, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /organizations/{id} without token returns 401")
    void should_return401_when_updateWithoutToken() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrganizationDto(id, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /organizations/{id} with valid token returns 200")
    void should_return200_when_deleteWithValidToken() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(organizationService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /organizations/{id} when not found returns 404")
    void should_return404_when_deleteNotFound() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new OrganizationNotFoundException("Organization with ID " + id + " not found"))
                .when(organizationService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /organizations/{id} when caller is not OWNER returns 403")
    void should_return403_when_deleteAccessDenied() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new OrganizationAccessDeniedException("Access denied: insufficient permissions in organization " + id))
                .when(organizationService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id).header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /organizations/{id} without token returns 401")
    void should_return401_when_deleteWithoutToken() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private OrganizationDto buildOrganizationDto(UUID id, UUID ownerId) {
        return new OrganizationDto(
                id,
                "My Organization",
                "An organization description",
                ownerId
        );
    }
}
