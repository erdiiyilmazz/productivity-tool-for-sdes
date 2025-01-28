package com.erdidev.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Value("${timemanager.openapi.dev-url}")
    private String devUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Time Manager API")
                        .description("Time Management Application with Task Scheduling")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Erdi")
                                .url("https://github.com/erdidev")));
    }
} 