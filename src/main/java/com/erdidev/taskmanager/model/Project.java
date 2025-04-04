package com.erdidev.taskmanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @OneToMany(mappedBy = "project")
    private Set<Category> categories = new HashSet<>();
    
    @OneToMany(mappedBy = "project")
    private Set<Task> tasks = new HashSet<>();
} 