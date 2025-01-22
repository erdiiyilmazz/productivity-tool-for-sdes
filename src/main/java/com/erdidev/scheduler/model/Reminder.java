package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.NotificationChannel;
import com.erdidev.scheduler.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDateTime reminderTime;

    @ElementCollection
    @CollectionTable(name = "reminder_notification_channels")
    @Enumerated(EnumType.STRING)
    private Set<NotificationChannel> notificationChannels;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}