package local.pms.taskservice.external.organization.client;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for the organization-service membership-check endpoint.
 * The caller's own JWT is forwarded by {@code TokenFeignRequestInterceptor},
 * so this asks organization-service "is the calling user a member of this org".
 */
@FeignClient(name = "organization-service", url = "${project-management-system.client.organization-service.url}")
public interface OrganizationFeignClient {

    /**
     * Retrieves the calling user's own membership in the given organization.
     *
     * @param organizationId the organization identifier
     * @return API response wrapping the caller's membership (404 if not a member)
     */
    @GetMapping("/{organizationId}/members/me")
    ApiResponseDto<OrganizationMemberResponseDto> getMyMembership(@PathVariable("organizationId") UUID organizationId);
}
