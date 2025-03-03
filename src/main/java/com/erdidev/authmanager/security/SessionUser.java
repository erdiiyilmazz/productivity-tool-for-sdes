package com.erdidev.authmanager.security;

import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SessionUser implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Set<String> roles = new HashSet<>();
    private boolean enabled;
    private boolean accountNonLocked;

    public static SessionUser fromUser(User user) {
        SessionUser sessionUser = new SessionUser();
        sessionUser.setId(user.getId());
        sessionUser.setUsername(user.getUsername());
        sessionUser.setPassword(user.getPassword());
        sessionUser.setEmail(user.getEmail());
        sessionUser.setFullName(user.getFullName());
        sessionUser.setEnabled(user.isEnabled());
        sessionUser.setAccountNonLocked(user.isAccountNonLocked());
        sessionUser.setRoles(user.getRoles().stream()
            .map(Role::getAuthority)
            .collect(Collectors.toSet()));
        return sessionUser;
    }

    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setFullName(this.fullName);
        user.setEnabled(this.enabled);
        user.setAccountNonLocked(this.accountNonLocked);
        return user;
    }
} 