package local.pms.organizationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.organizationservice.dto.AddMemberRequestDto;
import local.pms.organizationservice.dto.OrganizationMemberDto;
import local.pms.organizationservice.dto.UpdateMemberRoleRequestDto;

import local.pms.organizationservice.dto.api.response.ApiResponseDto;

import local.pms.organizationservice.service.OrganizationMemberService;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static local.pms.organizationservice.constant.VersionAPI.API_V1;

@Tag(name = "OrganizationMember", description = "Organization Membership REST API")
@RestController
@RequestMapping(API_V1 + "/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class OrganizationMemberRestController {

    private final OrganizationMemberService organizationMemberService;

    @Operation(
            summary = "Get the caller's own membership in an organization",
            description = "Used by other services to verify the calling user's organization access and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Caller's membership retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationMemberDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Organization not found or caller is not a member")
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationMemberDto> getMyMembership(@Parameter(description = "Organization identifier")
                                                                 @PathVariable(name = "organizationId") UUID organizationId) {
        return ApiResponseDto.buildSuccessResponse(organizationMemberService.getMyMembership(organizationId));
    }

    @Operation(summary = "Add a member to an organization", description = "The caller must be an OWNER or ADMIN member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member added successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationMemberDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid member data provided"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "User is already a member"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationMemberDto> addMember(@Parameter(description = "Organization identifier")
                                                           @PathVariable(name = "organizationId") UUID organizationId,
                                                           @Parameter(description = "Member to add")
                                                           @Valid @RequestBody AddMemberRequestDto request) {
        return ApiResponseDto.buildSuccessResponse(organizationMemberService.addMember(organizationId, request));
    }

    @Operation(
            summary = "Find all members of an organization",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc). The caller must be a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of members", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationMemberDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<OrganizationMemberDto> findAll(@Parameter(description = "Organization identifier")
                                               @PathVariable(name = "organizationId") UUID organizationId,
                                               @ParameterObject Pageable pageable) {
        return organizationMemberService.findAll(organizationId, pageable);
    }

    @Operation(summary = "Change a member's role", description = "The caller must be an OWNER member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationMemberDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid role provided or last OWNER would be removed"),
            @ApiResponse(responseCode = "404", description = "Organization or member not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(value = "/{memberId}/role", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationMemberDto> updateRole(@Parameter(description = "Organization identifier")
                                                             @PathVariable(name = "organizationId") UUID organizationId,
                                                             @Parameter(description = "Membership identifier to update")
                                                             @PathVariable(name = "memberId") UUID memberId,
                                                             @Parameter(description = "New role")
                                                             @Valid @RequestBody UpdateMemberRoleRequestDto request) {
        return ApiResponseDto.buildSuccessResponse(organizationMemberService.updateRole(organizationId, memberId, request));
    }

    @Operation(summary = "Remove a member from an organization", description = "The caller must be an OWNER/ADMIN member, or removing themselves")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "400", description = "Last OWNER cannot be removed"),
            @ApiResponse(responseCode = "404", description = "Organization or member not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(value = "/{memberId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> removeMember(@Parameter(description = "Organization identifier")
                                             @PathVariable(name = "organizationId") UUID organizationId,
                                             @Parameter(description = "Membership identifier to remove")
                                             @PathVariable(name = "memberId") UUID memberId) {
        organizationMemberService.removeMember(organizationId, memberId);
        return ApiResponseDto.buildSuccessResponse(null);
    }
}
