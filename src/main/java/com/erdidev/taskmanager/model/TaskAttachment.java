package com.erdidev.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "task_attachments")
public class TaskAttachment extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] content;
    
    private String extension;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
} 