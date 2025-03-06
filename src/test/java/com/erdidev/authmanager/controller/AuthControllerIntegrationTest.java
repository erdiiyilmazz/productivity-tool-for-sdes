package com.erdidev.authmanager.controller;

import com.erdidev.authmanager.dto.AuthRequest;
import com.erdidev.authmanager.dto.RegisterRequest;
import com.erdidev.authmanager.dto.UserDto;
import com.erdidev.authmanager.service.AuthService;
import com.erdidev.config.BaseTestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseTestConfiguration.class)
@Testcontainers
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthService authService;
    
    private MockMvc mockMvc;
    private UserDto testUserDto;
    private MockHttpSession session;
    
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .alwaysDo(print())
            .build();
            
        testUserDto = new UserDto();
        testUserDto.setId(999L);
        testUserDto.setUsername("integrationtest");
        testUserDto.setEmail("integration@test.com");
        
        session = new MockHttpSession();
        
        // Set up default mocks
        when(authService.register(any())).thenReturn(testUserDto);
        when(authService.authenticate(any(), any())).thenReturn(testUserDto);
        when(authService.getCurrentUser("integrationtest")).thenReturn(testUserDto);
        when(authService.getCurrentUser("testuser2")).thenReturn(testUserDto);
        when(authService.getCurrentUser("mockuser")).thenReturn(testUserDto);
    }

    @Test
    public void testBasicAuthFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtest");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("integrationtest"));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("integrationtest");
        authRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("integrationtest"));
    }

    @Test
    @WithMockUser(username = "integrationtest")
    public void testRegisterAndAuthenticateUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtest");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("integrationtest"));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("integrationtest");
        authRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("integrationtest"));

        mockMvc.perform(get("/api/v1/auth/me")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("integrationtest"));

        mockMvc.perform(post("/api/v1/auth/logout")
                .session(session))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser2")
    public void testAuthenticatedEndpoints() throws Exception {
        UserDto testUser2Dto = new UserDto();
        testUser2Dto.setId(123L);
        testUser2Dto.setUsername("testuser2");
        testUser2Dto.setEmail("test2@example.com");
        
        when(authService.getCurrentUser("testuser2")).thenReturn(testUser2Dto);
        
        mockMvc.perform(get("/api/v1/auth/me")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser2"));
    }
    
    @Test
    @WithMockUser(username = "mockuser")
    public void testMeEndpointWithMockUser() throws Exception {
        UserDto mockUserDto = new UserDto();
        mockUserDto.setId(456L);
        mockUserDto.setUsername("mockuser");
        mockUserDto.setEmail("mock@example.com");
        
        when(authService.getCurrentUser("mockuser")).thenReturn(mockUserDto);
        
        mockMvc.perform(get("/api/v1/auth/me")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("mockuser"));
    }
} 