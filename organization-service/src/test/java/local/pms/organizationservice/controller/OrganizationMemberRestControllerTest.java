package local.pms.organizationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.organizationservice.config.SecurityConfig;

import local.pms.organizationservice.config.jwt.JwtTokenProvider;

import local.pms.organizationservice.dto.AddMemberRequestDto;
import local.pms.organizationservice.dto.OrganizationMemberDto;
import local.pms.organizationservice.dto.UpdateMemberRoleRequestDto;

import local.pms.organizationservice.exception.LastOwnerRemovalException;
import local.pms.organizationservice.exception.DuplicateMembershipException;
import local.pms.organizationservice.exception.OrganizationNotFoundException;
import local.pms.organizationservice.exception.OrganizationAccessDeniedException;
import local.pms.organizationservice.exception.OrganizationMemberNotFoundException;

import local.pms.organizationservice.service.TokenService;
import local.pms.organizationservice.service.OrganizationMemberService;

import local.pms.organizationservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

@WebMvcTest(OrganizationMemberRestController.class)
@Import(SecurityConfig.class)
class OrganizationMemberRestControllerTest {

    private static final String BEARER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationMemberService organizationMemberService;

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
    @DisplayName("GET /organizations/{id}/members/me with valid token returns 200")
    void should_return200_when_getMyMembershipWithValidToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var dto = buildMemberDto(UUID.randomUUID(), organizationId, UUID.randomUUID(), OrganizationRoleType.ADMIN);
        when(organizationMemberService.getMyMembership(organizationId)).thenReturn(dto);

        mockMvc.perform(get(membersUrl(organizationId) + "/me").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /organizations/{id}/members/me when caller is not a member returns 404")
    void should_return404_when_getMyMembershipNotAMember() throws Exception {
        var organizationId = UUID.randomUUID();
        when(organizationMemberService.getMyMembership(organizationId))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + organizationId + " not found"));

        mockMvc.perform(get(membersUrl(organizationId) + "/me").header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /organizations/{id}/members/me without token returns 401")
    void should_return401_when_getMyMembershipWithoutToken() throws Exception {
        mockMvc.perform(get(membersUrl(UUID.randomUUID()) + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /organizations/{id}/members with valid body returns 200")
    void should_return200_when_addMemberWithValidBody() throws Exception {
        var organizationId = UUID.randomUUID();
        var request = new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER);
        var created = buildMemberDto(UUID.randomUUID(), organizationId, request.userId(), OrganizationRoleType.MEMBER);
        when(organizationMemberService.addMember(eq(organizationId), any(AddMemberRequestDto.class))).thenReturn(created);

        mockMvc.perform(post(membersUrl(organizationId))
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    @DisplayName("POST /organizations/{id}/members with missing userId returns 400")
    void should_return400_when_addMemberWithMissingUserId() throws Exception {
        var organizationId = UUID.randomUUID();
        var body = new AddMemberRequestDto(null, OrganizationRoleType.MEMBER);

        mockMvc.perform(post(membersUrl(organizationId))
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /organizations/{id}/members without token returns 401")
    void should_return401_when_addMemberWithoutToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var request = new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER);

        mockMvc.perform(post(membersUrl(organizationId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /organizations/{id}/members when organization not found returns 404")
    void should_return404_when_addMemberOrganizationNotFound() throws Exception {
        var organizationId = UUID.randomUUID();
        var request = new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER);
        when(organizationMemberService.addMember(eq(organizationId), any(AddMemberRequestDto.class)))
                .thenThrow(new OrganizationNotFoundException("Organization with ID " + organizationId + " not found"));

        mockMvc.perform(post(membersUrl(organizationId))
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /organizations/{id}/members when user already a member returns 409")
    void should_return409_when_addMemberDuplicate() throws Exception {
        var organizationId = UUID.randomUUID();
        var request = new AddMemberRequestDto(UUID.randomUUID(), OrganizationRoleType.MEMBER);
        when(organizationMemberService.addMember(eq(organizationId), any(AddMemberRequestDto.class)))
                .thenThrow(new DuplicateMembershipException("User is already a member"));

        mockMvc.perform(post(membersUrl(organizationId))
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /organizations/{id}/members with valid token returns 200 with page")
    void should_return200_when_findAllWithValidToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var dto = buildMemberDto(UUID.randomUUID(), organizationId, UUID.randomUUID(), OrganizationRoleType.OWNER);
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(organizationMemberService.findAll(eq(organizationId), any())).thenReturn(page);

        mockMvc.perform(get(membersUrl(organizationId)).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("OWNER"));
    }

    @Test
    @DisplayName("GET /organizations/{id}/members without token returns 401")
    void should_return401_when_findAllWithoutToken() throws Exception {
        mockMvc.perform(get(membersUrl(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /organizations/{id}/members/{memberId}/role with valid body returns 200")
    void should_return200_when_updateRoleWithValidBody() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var request = new UpdateMemberRoleRequestDto(OrganizationRoleType.ADMIN);
        var updated = buildMemberDto(memberId, organizationId, UUID.randomUUID(), OrganizationRoleType.ADMIN);
        when(organizationMemberService.updateRole(eq(organizationId), eq(memberId), any(UpdateMemberRoleRequestDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put(membersUrl(organizationId) + "/" + memberId + "/role")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT /organizations/{id}/members/{memberId}/role when member not found returns 404")
    void should_return404_when_updateRoleMemberNotFound() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var request = new UpdateMemberRoleRequestDto(OrganizationRoleType.ADMIN);
        when(organizationMemberService.updateRole(eq(organizationId), eq(memberId), any(UpdateMemberRoleRequestDto.class)))
                .thenThrow(new OrganizationMemberNotFoundException("Member not found"));

        mockMvc.perform(put(membersUrl(organizationId) + "/" + memberId + "/role")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /organizations/{id}/members/{memberId}/role when caller not OWNER returns 403")
    void should_return403_when_updateRoleAccessDenied() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var request = new UpdateMemberRoleRequestDto(OrganizationRoleType.ADMIN);
        when(organizationMemberService.updateRole(eq(organizationId), eq(memberId), any(UpdateMemberRoleRequestDto.class)))
                .thenThrow(new OrganizationAccessDeniedException("Access denied"));

        mockMvc.perform(put(membersUrl(organizationId) + "/" + memberId + "/role")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /organizations/{id}/members/{memberId}/role when it would remove the last OWNER returns 400")
    void should_return400_when_updateRoleRemovesLastOwner() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        var request = new UpdateMemberRoleRequestDto(OrganizationRoleType.MEMBER);
        when(organizationMemberService.updateRole(eq(organizationId), eq(memberId), any(UpdateMemberRoleRequestDto.class)))
                .thenThrow(new LastOwnerRemovalException("Cannot change role: must have at least one OWNER"));

        mockMvc.perform(put(membersUrl(organizationId) + "/" + memberId + "/role")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /organizations/{id}/members/{memberId}/role without token returns 401")
    void should_return401_when_updateRoleWithoutToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();

        mockMvc.perform(put(membersUrl(organizationId) + "/" + memberId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateMemberRoleRequestDto(OrganizationRoleType.ADMIN))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /organizations/{id}/members/{memberId} with valid token returns 200")
    void should_return200_when_removeMemberWithValidToken() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        doNothing().when(organizationMemberService).removeMember(organizationId, memberId);

        mockMvc.perform(delete(membersUrl(organizationId) + "/" + memberId).header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /organizations/{id}/members/{memberId} when member not found returns 404")
    void should_return404_when_removeMemberNotFound() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        doThrow(new OrganizationMemberNotFoundException("Member not found"))
                .when(organizationMemberService).removeMember(organizationId, memberId);

        mockMvc.perform(delete(membersUrl(organizationId) + "/" + memberId).header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /organizations/{id}/members/{memberId} when it would remove the last OWNER returns 400")
    void should_return400_when_removeLastOwner() throws Exception {
        var organizationId = UUID.randomUUID();
        var memberId = UUID.randomUUID();
        doThrow(new LastOwnerRemovalException("Cannot remove member: must have at least one OWNER"))
                .when(organizationMemberService).removeMember(organizationId, memberId);

        mockMvc.perform(delete(membersUrl(organizationId) + "/" + memberId).header("Authorization", BEARER))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /organizations/{id}/members/{memberId} without token returns 401")
    void should_return401_when_removeMemberWithoutToken() throws Exception {
        mockMvc.perform(delete(membersUrl(UUID.randomUUID()) + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private String membersUrl(UUID organizationId) {
        return "/api/v1/organizations/" + organizationId + "/members";
    }

    private OrganizationMemberDto buildMemberDto(UUID id, UUID organizationId, UUID userId, OrganizationRoleType role) {
        return new OrganizationMemberDto(id, organizationId, userId, role);
    }
}
