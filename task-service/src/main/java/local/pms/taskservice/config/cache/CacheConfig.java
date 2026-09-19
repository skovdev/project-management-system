package local.pms.taskservice.config.cache;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;

import org.springframework.cache.annotation.CachingConfigurer;

import org.springframework.cache.interceptor.CacheErrorHandler;

import org.springframework.context.annotation.Configuration;

/**
 * Makes the Spring Cache abstraction degrade gracefully: if Redis is unreachable, a cache
 * get/put/evict failure is logged and swallowed instead of failing the request, so annotated
 * methods fall through to their normal (uncached) execution.
 */
@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache GET failed for cache='{}', key='{}' - falling back to source", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache PUT failed for cache='{}', key='{}' - value not cached", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache EVICT failed for cache='{}', key='{}'", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache CLEAR failed for cache='{}'", cache.getName(), exception);
            }
        };
    }
}
