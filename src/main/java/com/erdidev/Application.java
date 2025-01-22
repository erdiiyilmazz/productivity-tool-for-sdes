package com.erdidev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.erdidev.timemanager",
    "com.erdidev.scheduler",
    "com.erdidev.common"
})
@EntityScan(basePackages = {
    "com.erdidev.timemanager.model",
    "com.erdidev.scheduler.model",
    "com.erdidev.common.model"
})
@EnableJpaRepositories(basePackages = {
    "com.erdidev.timemanager.repository",
    "com.erdidev.scheduler.repository"
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 