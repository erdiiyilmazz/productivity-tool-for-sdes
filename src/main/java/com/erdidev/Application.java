package com.erdidev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.erdidev.timemanager",
    "com.erdidev.common",
    "com.erdidev.scheduler"
})
@EntityScan(basePackages = {
    "com.erdidev.timemanager.model",
    "com.erdidev.scheduler.model"
})
@EnableJpaRepositories(basePackages = {
    "com.erdidev.timemanager.repository",
    "com.erdidev.scheduler.repository"
})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 