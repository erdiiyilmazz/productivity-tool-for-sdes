package com.erdidev.timetracker.exception;

public class TimeEntryNotFoundException extends RuntimeException {
    public TimeEntryNotFoundException(Long id) {
        super("Time entry not found with id: " + id);
    }
} 