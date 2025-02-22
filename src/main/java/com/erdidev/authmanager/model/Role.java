package com.erdidev.authmanager.model;

import com.erdidev.taskmanager.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity implements GrantedAuthority {
    
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleType name;
    
    private String description;

    @Override
    public String getAuthority() {
        return name.name();
    }
} 