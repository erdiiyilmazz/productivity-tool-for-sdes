package com.erdidev.scheduler.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationMessage {
    private final String content;
    private final LocalDateTime timestamp;
    private final String type = "REMINDER";

    public NotificationMessage(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
} 