package com.erdidev.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${taskmanager.openapi.dev-url}")
    private String devUrl;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setName("Erdi");
        contact.setUrl("https://github.com/erdiiyilmazz");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Time Manager API")
                .version("1.0")
                .contact(contact)
                .description("Time Management Application with Task Scheduling")
                .termsOfService("https://www.example.com/terms")
                .license(mitLicense);

        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("Session cookie for authentication");

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(new Components().addSecuritySchemes("cookieAuth", cookieAuth));

        // Only add global security requirement in non-development environments
        if (!"dev".equals(activeProfile) && !"default".equals(activeProfile)) {
            openAPI.addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
        }

        return openAPI;
    }
} 