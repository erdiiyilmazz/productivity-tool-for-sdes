package com.erdidev.timemanager.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
} 