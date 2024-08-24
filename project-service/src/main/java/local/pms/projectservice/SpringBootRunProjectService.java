package local.pms.projectservice;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SpringBootRunProjectService {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRunProjectService.class, args);
    }
}
