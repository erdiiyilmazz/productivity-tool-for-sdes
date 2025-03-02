package com.erdidev.scheduler.service.notification;

import com.erdidev.scheduler.dto.NotificationMessage;
import com.erdidev.scheduler.exception.NotificationDeliveryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationStrategyTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationStrategy notificationStrategy;

    @Test
    void sendNotification_Success() {
        String message = "Test notification message";
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationMessage.class));
        
        assertDoesNotThrow(() -> {
            notificationStrategy.sendNotification(message);
        });
        
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationMessage.class));
    }

    @Test
    void sendNotification_MessagingTemplateFails_ThrowsException() {
        String message = "Test notification message";
        doThrow(new RuntimeException("Failed to send message"))
            .when(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationMessage.class));
        
        assertThrows(NotificationDeliveryException.class, () -> {
            notificationStrategy.sendNotification(message);
        });
    }
} 