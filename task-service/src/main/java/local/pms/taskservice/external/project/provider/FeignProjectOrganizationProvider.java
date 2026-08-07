package local.pms.taskservice.external.project.provider;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.taskservice.exception.TaskAccessDeniedException;

import local.pms.taskservice.external.project.client.ProjectFeignClient;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * project-service-backed implementation of {@link ProjectOrganizationProvider}, protected
 * by a circuit breaker and retry policy. Fails closed: any failure to resolve the project's
 * organization (not found, timeout, circuit open) denies access.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeignProjectOrganizationProvider implements ProjectOrganizationProvider {

    private final ProjectFeignClient projectFeignClient;

    @Override
    @CircuitBreaker(name = "projectOrganizationResolution", fallbackMethod = "fallbackResolveOrganizationId")
    @Retry(name = "projectOrganizationResolution", fallbackMethod = "fallbackResolveOrganizationId")
    public UUID resolveOrganizationId(UUID projectId) {
        var response = projectFeignClient.getOrganizationId(projectId);
        if (response == null || response.getData() == null) {
            log.warn("project-service returned no organization for projectId='{}'", projectId);
            throw new TaskAccessDeniedException(
                    "Access denied: unable to resolve organization for project with ID " + projectId);
        }
        return response.getData().organizationId();
    }

    private UUID fallbackResolveOrganizationId(UUID projectId, Throwable t) {
        log.error("Failed to resolve organization for projectId='{}'. Denying access.", projectId, t);
        throw new TaskAccessDeniedException(
                "Access denied: unable to resolve organization for project with ID " + projectId);
    }
}
