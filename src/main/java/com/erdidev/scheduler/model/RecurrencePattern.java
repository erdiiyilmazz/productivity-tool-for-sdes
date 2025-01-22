package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Embeddable
@Getter
@Setter
public class RecurrencePattern {
    @Column
    @Enumerated(EnumType.STRING)
    private RecurrenceType type;
    
    @Column
    private Integer interval;
    
    @ElementCollection
    private Set<DayOfWeek> daysOfWeek;
    
    @Column
    private Integer dayOfMonth;
    
    @Column
    private LocalDateTime endDate;
    
    @Column
    private Integer occurrences;
} 