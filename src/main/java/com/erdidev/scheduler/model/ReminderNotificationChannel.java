package com.erdidev.scheduler.model;

import com.erdidev.scheduler.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
public class ReminderNotificationChannel {
    @Column(name = "notification_channels", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel notificationChannel;

    @Column(name = "channel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channelType;

    @Column(name = "channel_details", nullable = false)
    private String channelDetails = "default";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 