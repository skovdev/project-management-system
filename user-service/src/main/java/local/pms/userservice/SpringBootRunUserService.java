package local.pms.userservice;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SpringBootRunUserService {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRunUserService.class, args);
    }
}
