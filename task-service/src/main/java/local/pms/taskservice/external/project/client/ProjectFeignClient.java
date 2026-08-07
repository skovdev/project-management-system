package local.pms.taskservice.external.project.client;

import local.pms.taskservice.dto.api.response.ApiResponseDto;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for the project-service projectId to organizationId lookup endpoint.
 * The caller's own JWT is forwarded by {@code TokenFeignRequestInterceptor}.
 */
@FeignClient(name = "project-service", url = "${project-management-system.client.project-service.url}")
public interface ProjectFeignClient {

    /**
     * Resolves the organization a project belongs to.
     *
     * @param projectId the project identifier
     * @return API response wrapping the project's organization identifier (404 if project not found)
     */
    @GetMapping("/{projectId}/organization-id")
    ApiResponseDto<ProjectOrganizationResponseDto> getOrganizationId(@PathVariable("projectId") UUID projectId);
}
