package local.pms.apigateway;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringBootRunApiGateway {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRunApiGateway.class, args);
    }
}
