package com.erdidev.authmanager.service;

import com.erdidev.authmanager.dto.AuthRequest;
import com.erdidev.authmanager.dto.RegisterRequest;
import com.erdidev.authmanager.dto.UserDto;
import com.erdidev.authmanager.exception.AuthenticationException;
import com.erdidev.authmanager.exception.UserNotFoundException;
import com.erdidev.authmanager.mapper.UserMapper;
import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.RoleType;
import com.erdidev.authmanager.model.User;
import com.erdidev.authmanager.repository.RoleRepository;
import com.erdidev.authmanager.repository.UserRepository;
import com.erdidev.authmanager.security.UserPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserDto register(RegisterRequest request) {
        log.debug("Attempting to register user with username: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.debug("Username already exists: {}", request.getUsername());
            throw new AuthenticationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.debug("Email already exists: {}", request.getEmail());
            throw new AuthenticationException("Email already exists");
        }

        // Create default role if it doesn't exist
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
            .orElseGet(() -> {
                log.debug("Creating default USER role as it doesn't exist");
                Role newRole = new Role();
                newRole.setName(RoleType.ROLE_USER);
                newRole.setDescription("Default user role");
                return roleRepository.save(newRole);
            });

        User user = new User();
        user.setUsername(request.getUsername());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("Password encoded successfully: {}", encodedPassword);
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        
        user.setRoles(Collections.singleton(userRole));
        
        log.debug("Saving user to database");
        User savedUser = userRepository.save(user);
        log.debug("User saved successfully with ID: {}", savedUser.getId());
        
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto authenticate(AuthRequest request, HttpSession session) {
        try {
            log.debug("Attempting authentication for username: {}", request.getUsername());
            
            // First check if user exists
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.debug("User not found: {}", request.getUsername());
                    return new AuthenticationException("Invalid username or password");
                });
            
            log.debug("User found, stored password hash: {}", user.getPassword());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            user = principal.getUser();

            if (!user.isEnabled()) {
                log.debug("Account is disabled for user: {}", user.getUsername());
                throw new AuthenticationException("Account is disabled");
            }
            if (!user.isAccountNonLocked()) {
                log.debug("Account is locked for user: {}", user.getUsername());
                throw new AuthenticationException("Account is locked");
            }

            sessionService.createSession(session.getId(), user);
            log.debug("User {} successfully authenticated and session created", user.getUsername());
            return userMapper.toDto(user);
            
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for username: {} - Bad credentials", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for username: {} - {}", request.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for username: {}", request.getUsername(), e);
            throw new AuthenticationException("Authentication failed");
        }
    }

    @Transactional
    public void logout(HttpSession session) {
        sessionService.invalidateSession(session.getId());
        session.invalidate();
        log.debug("User session invalidated: {}", session.getId());
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        log.debug("Getting current user: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll().stream()
            .map(userMapper::toDto)
            .collect(Collectors.toList());
    }
} 