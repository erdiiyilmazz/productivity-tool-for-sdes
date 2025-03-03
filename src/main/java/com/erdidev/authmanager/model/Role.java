package com.erdidev.authmanager.model;

import com.erdidev.taskmanager.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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