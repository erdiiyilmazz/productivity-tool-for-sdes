package com.erdidev.timemanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @OneToMany(mappedBy = "project")
    private Set<Category> categories = new HashSet<>();
    
    @OneToMany(mappedBy = "project")
    private Set<Task> tasks = new HashSet<>();
} 