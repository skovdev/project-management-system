package local.pms.taskservice.external.organization.provider;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.taskservice.exception.TaskAccessDeniedException;

import local.pms.taskservice.external.organization.client.OrganizationFeignClient;

import local.pms.taskservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * organization-service-backed implementation of {@link OrganizationAccessProvider}, protected
 * by a circuit breaker and retry policy. Fails closed: any failure to positively confirm
 * membership (not-a-member, timeout, circuit open) denies access.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeignOrganizationAccessProvider implements OrganizationAccessProvider {

    private final OrganizationFeignClient organizationFeignClient;

    @Override
    @CircuitBreaker(name = "organizationMembershipVerification", fallbackMethod = "fallbackVerifyMembership")
    @Retry(name = "organizationMembershipVerification", fallbackMethod = "fallbackVerifyMembership")
    public OrganizationRoleType verifyMembership(UUID organizationId) {
        var response = organizationFeignClient.getMyMembership(organizationId);
        if (response == null || response.getData() == null) {
            log.warn("organization-service returned a null membership for organizationId='{}'", organizationId);
            throw new TaskAccessDeniedException(
                    "Access denied: you are not a member of organization with ID " + organizationId);
        }
        return response.getData().role();
    }

    private OrganizationRoleType fallbackVerifyMembership(UUID organizationId, Throwable t) {
        log.error("Failed to verify membership for organizationId='{}'. Denying access.", organizationId, t);
        throw new TaskAccessDeniedException(
                "Access denied: unable to verify membership for organization with ID " + organizationId);
    }
}
