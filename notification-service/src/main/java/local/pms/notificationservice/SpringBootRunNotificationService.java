package local.pms.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the notification-service.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SpringBootRunNotificationService {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRunNotificationService.class, args);
    }
}
