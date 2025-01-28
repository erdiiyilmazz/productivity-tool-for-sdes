package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.timemanager.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Schedule extends BaseEntity {
    @Column(nullable = false)
    private Long taskId;
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;
    
    @Column
    private String timeZone;
    
    @Column
    private String title;
    
    @Column
    private String description;
} 