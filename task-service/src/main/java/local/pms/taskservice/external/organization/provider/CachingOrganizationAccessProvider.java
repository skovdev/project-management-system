package local.pms.taskservice.external.organization.provider;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.service.TokenService;

import local.pms.taskservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.context.annotation.Primary;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Caches successful membership verifications for a short TTL (see
 * {@code spring.cache.redis.time-to-live}) so that every task/comment operation for the same
 * user/organization pair doesn't re-hit organization-service. Denials and failures are never
 * cached — {@link FeignOrganizationAccessProvider} throws rather than returning on those paths,
 * and {@code @Cacheable} never caches a thrown exception — so a revoked membership stops working
 * again within one TTL window even though there is no active cache-invalidation event yet.
 */
@Primary
@Component
@RequiredArgsConstructor
public class CachingOrganizationAccessProvider implements OrganizationAccessProvider {

    private final FeignOrganizationAccessProvider delegate;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Cacheable(cacheNames = "organizationMembership", key = "#organizationId + ':' + root.target.currentAuthUserId()")
    public OrganizationRoleType verifyMembership(UUID organizationId) {
        return delegate.verifyMembership(organizationId);
    }

    /**
     * Public only so the {@code @Cacheable} key SpEL above (evaluated via reflection on
     * {@code root.target}) can call it; not part of this class's real API.
     */
    public UUID currentAuthUserId() {
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}
