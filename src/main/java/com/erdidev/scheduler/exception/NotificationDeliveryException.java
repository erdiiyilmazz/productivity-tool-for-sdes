package com.erdidev.scheduler.exception;

public class NotificationDeliveryException extends RuntimeException {
    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
} 