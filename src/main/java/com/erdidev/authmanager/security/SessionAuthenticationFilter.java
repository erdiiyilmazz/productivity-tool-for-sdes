package com.erdidev.authmanager.security;

import com.erdidev.authmanager.model.User;
import com.erdidev.authmanager.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private static final String SPRING_SECURITY_CONTEXT_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                log.debug("Processing session: {}", session.getId());
                
                // First try to get the security context from the session
                SecurityContext securityContext = (SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
                
                if (securityContext != null && securityContext.getAuthentication() != null) {
                    log.debug("Found existing security context");
                    SecurityContextHolder.setContext(securityContext);
                } else {
                    // If no security context, try to get the user from our session service
                    User user = sessionService.getSession(session.getId());
                    if (user != null) {
                        log.debug("Creating new security context for user: {}", user.getUsername());
                        UserPrincipal principal = new UserPrincipal(SessionUser.fromUser(user));
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        
                        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
                        newContext.setAuthentication(authentication);
                        SecurityContextHolder.setContext(newContext);
                        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, newContext);
                        
                        sessionService.refreshSession(session.getId());
                    }
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error in session authentication filter", e);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
} 