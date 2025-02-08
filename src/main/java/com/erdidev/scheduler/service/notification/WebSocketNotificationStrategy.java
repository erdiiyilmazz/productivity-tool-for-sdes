package com.erdidev.scheduler.service.notification;

import com.erdidev.scheduler.dto.NotificationMessage;
import com.erdidev.scheduler.exception.NotificationDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationStrategy implements NotificationStrategy {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(String message) {
        try {
            NotificationMessage notification = new NotificationMessage(
                String.format("🔔 REMINDER: %s\n⏰ Time: %s", 
                    message,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                )
            );
            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            throw new NotificationDeliveryException("Failed to send WebSocket notification", e);
        }
    }
} 