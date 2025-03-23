package com.erdidev.authmanager.config;

import com.erdidev.authmanager.security.SessionAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import com.erdidev.authmanager.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui/**", 
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/webjars/**",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml"
    };
    
    private static final String[] STATIC_RESOURCES = {
        "/static/**",
        "/*.css",
        "/*.js",
        "/*.html",
        "/favicon.ico",
        "/swagger-ui-custom.css"
    };
    
    private static final String[] PUBLIC_API_PATHS = {
        "/api/v1/auth/register", 
        "/api/v1/auth/login",
        "/api/v1/auth/logout",
        "/api/v1/auth/simple-login",
        "/api/v1/auth/check",
        "/api/v1/auth/session-info",
        "/api/v1/auth/me",
        "/api/v1/auth/debug",
        "/api/v1/projects/**",
        "/api/v1/tasks/**",                // Task management
        "/api/v1/categories/**",           // Categories
        "/api/v1/attachments/**",          // Task attachments
        "/api/v1/schedules/**",            // Schedules
        "/api/v1/reminders/**",            // Reminders 
        "/api/v1/time-entries/**"          // Time tracking
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring security filter chain...");
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false))
            .securityContext(context -> context
                .requireExplicitSave(false)
                .securityContextRepository(securityContextRepository()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_API_PATHS).permitAll()
                .requestMatchers(SWAGGER_PATHS).permitAll()
                .requestMatchers(STATIC_RESOURCES).permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.debug("Security configuration completed");
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
               .passwordEncoder(passwordEncoder());
        return builder.build();
    }
} 