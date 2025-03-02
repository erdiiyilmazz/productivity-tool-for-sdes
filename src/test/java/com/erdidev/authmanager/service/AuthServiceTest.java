package com.erdidev.authmanager.service;

import com.erdidev.authmanager.dto.AuthRequest;
import com.erdidev.authmanager.dto.RegisterRequest;
import com.erdidev.authmanager.dto.UserDto;
import com.erdidev.authmanager.exception.AuthenticationException;
import com.erdidev.authmanager.mapper.UserMapper;
import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.RoleType;
import com.erdidev.authmanager.model.User;
import com.erdidev.authmanager.repository.RoleRepository;
import com.erdidev.authmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import com.erdidev.authmanager.security.UserPrincipal;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpSession httpSession;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleType.ROLE_USER);
        
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(testUser);  // Create UserPrincipal
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        lenient().when(authentication.getPrincipal()).thenReturn(userPrincipal);
        lenient().when(authenticationManager.authenticate(any())).thenReturn(authentication);
    }

    @Test
    void testRegisterUser() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(1L);
        expectedUserDto.setUsername("testuser");
        when(userMapper.toDto(any(User.class))).thenReturn(expectedUserDto);
        
        UserDto result = authService.register(registerRequest);
        
        assertNotNull(result);
        assertEquals(expectedUserDto.getId(), result.getId());
        assertEquals(expectedUserDto.getUsername(), result.getUsername());
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUserWithExistingUsername() {
        registerRequest.setUsername("testuser");  // Same as testUser.getUsername()
        registerRequest.setEmail("new@example.com");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        
        assertThrows(AuthenticationException.class, () -> authService.register(registerRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateUser() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        
        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(1L);
        expectedUserDto.setUsername("testuser");
        when(userMapper.toDto(testUser)).thenReturn(expectedUserDto);
        
        UserDto result = authService.authenticate(authRequest, httpSession);
        
        assertNotNull(result);
        assertEquals(expectedUserDto.getId(), result.getId());
        assertEquals(expectedUserDto.getUsername(), result.getUsername());
        verify(sessionService).createSession(any(), eq(testUser));
    }

    @Test
    void testAuthenticateUserInvalidCredentials() {
        when(userRepository.findByUsername(authRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.getPassword(), testUser.getPassword())).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationException("Invalid credentials"));
        
        assertThrows(AuthenticationException.class, () -> authService.authenticate(authRequest, httpSession));
        
        verify(sessionService, never()).createSession(any(), any());
    }

    @Test
    void testGetCurrentUser() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        
        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(1L);
        expectedUserDto.setUsername("testuser");
        when(userMapper.toDto(testUser)).thenReturn(expectedUserDto);
        
        UserDto result = authService.getCurrentUser(testUser.getUsername());
        
        assertNotNull(result);
        assertEquals(expectedUserDto.getId(), result.getId());
        assertEquals(expectedUserDto.getUsername(), result.getUsername());
    }

    @Test
    void testGetCurrentUserNotFound() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        
        assertThrows(AuthenticationException.class, () -> authService.getCurrentUser(testUser.getUsername()));
    }
} 