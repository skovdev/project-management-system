package local.pms.taskservice;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SpringBootRunTaskService {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRunTaskService.class, args);
    }
}
