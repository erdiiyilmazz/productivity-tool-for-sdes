package com.erdidev.scheduler.exception;

public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(Long id) {
        super("Schedule not found with id: " + id);
    }
} 