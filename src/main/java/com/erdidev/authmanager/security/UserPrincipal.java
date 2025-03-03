package com.erdidev.authmanager.security;

import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPrincipal implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final SessionUser sessionUser;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.sessionUser = SessionUser.fromUser(user);
        this.authorities = user.getRoles().stream()
            .map(Role::getAuthority)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    public UserPrincipal(SessionUser sessionUser) {
        this.sessionUser = sessionUser;
        this.authorities = sessionUser.getRoles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return sessionUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sessionUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return sessionUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sessionUser.isEnabled();
    }

    public User toUser() {
        return sessionUser.toUser();
    }

    public User getUser() {
        return toUser();
    }
} 