package com.erdidev.timemanager.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_attachments")
@EqualsAndHashCode(callSuper = true)
public class TaskAttachment extends BaseEntity {
    private String fileName;
    private String fileType;
    private String url;
    
    @Enumerated(EnumType.STRING)
    private AttachmentType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
} 