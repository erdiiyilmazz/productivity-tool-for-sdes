package com.erdidev.timetracker.model;

import com.erdidev.taskmanager.model.BaseEntity;
import com.erdidev.taskmanager.model.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
public class TimeEntry extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "is_billable")
    private boolean billable = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimeEntryStatus status = TimeEntryStatus.RUNNING;

    @PrePersist
    @PreUpdate
    public void calculateDuration() {
        if (endTime != null && startTime != null) {
            this.durationSeconds = Duration.between(startTime, endTime).getSeconds();
            this.status = TimeEntryStatus.COMPLETED;
        }
    }
} 