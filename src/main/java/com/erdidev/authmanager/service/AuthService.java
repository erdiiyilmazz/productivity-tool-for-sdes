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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        
        // Assign default USER role
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Collections.singleton(userRole));
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto authenticate(AuthRequest request, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();

            if (!user.isEnabled()) {
                throw new AuthenticationException("Account is disabled");
            }
            if (!user.isAccountNonLocked()) {
                throw new AuthenticationException("Account is locked");
            }

            sessionService.createSession(session.getId(), user);
            log.debug("User {} successfully authenticated", user.getUsername());
            return userMapper.toDto(user);
            
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for username: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
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
        return userRepository.findByUsername(username)
            .map(userMapper::toDto)
            .orElseThrow(() -> new AuthenticationException("User not found"));
    }
} 