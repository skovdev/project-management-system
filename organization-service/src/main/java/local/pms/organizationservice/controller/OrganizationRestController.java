package local.pms.organizationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.organizationservice.dto.OrganizationDto;

import local.pms.organizationservice.dto.api.response.ApiResponseDto;

import local.pms.organizationservice.service.OrganizationService;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

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

@Tag(name = "Organization", description = "Organization REST API")
@RestController
@RequestMapping(API_V1 + "/organizations")
@RequiredArgsConstructor
public class OrganizationRestController {

    private final OrganizationService organizationService;

    @Operation(summary = "Create a new organization", description = "The caller is automatically added as an OWNER member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid organization data provided"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationDto> create(@Parameter(description = "Organization data to create a new organization")
                                                  @Valid @RequestBody OrganizationDto organizationDto) {
        return ApiResponseDto.buildSuccessResponse(organizationService.create(organizationDto));
    }

    @Operation(
            summary = "Find all organizations the caller belongs to",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of organizations", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationDto.class))
            }),
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<OrganizationDto> findAll(@ParameterObject Pageable pageable) {
        return organizationService.findAll(pageable);
    }

    @Operation(summary = "Find an organization by identifier", description = "The caller must be a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization details retrieved successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationDto> findById(@Parameter(description = "Organization identifier to retrieve details")
                                                    @PathVariable(name = "organizationId") UUID organizationId) {
        return ApiResponseDto.buildSuccessResponse(organizationService.findById(organizationId));
    }

    @Operation(summary = "Update an existing organization", description = "The caller must be an OWNER or ADMIN member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrganizationDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid organization data provided"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<OrganizationDto> update(@Parameter(description = "Organization identifier to update")
                                                  @PathVariable(name = "organizationId") UUID organizationId,
                                                  @Parameter(description = "Updated organization data")
                                                  @Valid @RequestBody OrganizationDto organizationDto) {
        return ApiResponseDto.buildSuccessResponse(organizationService.update(organizationId, organizationDto));
    }

    @Operation(summary = "Delete an organization by identifier", description = "The caller must be an OWNER member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(value = "/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Void> delete(@Parameter(description = "Organization identifier to delete")
                                       @PathVariable(name = "organizationId") UUID organizationId) {
        organizationService.delete(organizationId);
        return ApiResponseDto.buildSuccessResponse(null);
    }
}
