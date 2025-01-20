package com.erdidev.timemanager.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<String> VALID_PROJECT_SORT_FIELDS = 
        Set.of("createdAt", "name", "updatedAt");
    
    private static final Set<String> VALID_CATEGORY_SORT_FIELDS = 
        Set.of("createdAt", "name", "updatedAt");
    
    private static final Set<String> VALID_SORT_DIRECTIONS = 
        Set.of("asc", "desc");

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        log.error("Task not found", ex);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(
                new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", LocalDateTime.now(), errors),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument", ex);
        return new ResponseEntity<>(
                new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage(),
                    LocalDateTime.now()
                ),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFoundException(ProjectNotFoundException ex) {
        log.error("Project not found", ex);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND);
    }

    private void validateSortParams(String field, String direction, Set<String> validFields) {
        if (!validFields.contains(field.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid sort field. Valid options are: " + String.join(", ", validFields));
        }
        if (!VALID_SORT_DIRECTIONS.contains(direction.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid sort direction. Use either 'asc' or 'desc'");
        }
    }

    record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
    record ValidationErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {}
} 