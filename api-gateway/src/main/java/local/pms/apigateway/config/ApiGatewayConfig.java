package local.pms.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.cloud.gateway.route.RouteLocator;

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator configureRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri(serviceHosts().get("user-service")))
                .route("project-service", r -> r.path("/api/v1/projects/**")
                        .uri(serviceHosts().get("profile-service")))
                .route("task-service", r -> r.path("/api/v1/task/**")
                        .uri(serviceHosts().get("profile-service")))
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "project.management.system.api-gateway.route.hosts")
    public Map<String, String> serviceHosts() {
        return new HashMap<>();
    }
}
