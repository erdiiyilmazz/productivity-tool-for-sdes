package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.taskmanager.model.BaseEntity;
import com.erdidev.taskmanager.model.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Schedule extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column
    private String timeZone;
    
    @Column
    private String title;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
} 