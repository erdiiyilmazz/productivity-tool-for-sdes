package com.erdidev.timemanager.exception;

public class TaskAttachmentNotFoundException extends RuntimeException {
    public TaskAttachmentNotFoundException(Long id) {
        super("Task attachment not found with id: " + id);
    }
} 