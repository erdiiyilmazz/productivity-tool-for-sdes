package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.NotificationChannel;
import com.erdidev.scheduler.enums.ReminderStatus;
import com.erdidev.timemanager.model.BaseEntity;
import com.erdidev.timemanager.model.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
public class Reminder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private LocalDateTime reminderTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel type = NotificationChannel.WEBSOCKET;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status = ReminderStatus.PENDING;

    @ElementCollection
    @CollectionTable(
        name = "reminder_notification_channels",
        joinColumns = @JoinColumn(name = "reminder_id")
    )
    private Set<ReminderNotificationChannel> notificationChannels;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String message;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (notificationChannels == null || notificationChannels.isEmpty()) {
            ReminderNotificationChannel channel = new ReminderNotificationChannel();
            channel.setNotificationChannel(NotificationChannel.WEBSOCKET);
            channel.setChannelType(NotificationChannel.WEBSOCKET);
            notificationChannels = Set.of(channel);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}