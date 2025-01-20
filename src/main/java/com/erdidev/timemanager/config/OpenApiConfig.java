package com.erdidev.timemanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI timeManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Time Manager API")
                        .description("Time Management Application API Documentation")
                        .version("1.0"));
    }
} 