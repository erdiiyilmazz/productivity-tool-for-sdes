package com.erdidev.timemanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {
        @Column(nullable = false)
        private String title;

        @Column(length = 1000)
        private String description;

        @Enumerated(EnumType.STRING)
        private TaskStatus status;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category;

        @ManyToOne
        private Project project;
        
        @Enumerated(EnumType.STRING)
        private Priority priority;
        private LocalDateTime dueDate;
        
        @OneToMany(cascade = CascadeType.ALL)
        private Set<TaskAttachment> attachments = new HashSet<>();
} 