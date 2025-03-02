package com.erdidev.timetracker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice(basePackages = "com.erdidev.timetracker")
@Component
public class GlobalExceptionHandler {

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTimeEntryNotFoundException(TimeEntryNotFoundException ex) {
        log.error("Time entry not found", ex);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
                HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(TimeTrackingException.class)
    public ResponseEntity<ErrorResponse> handleTimeTrackingException(TimeTrackingException ex) {
        log.error("Time tracking error", ex);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now()),
                HttpStatus.BAD_REQUEST);
    }

    record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
} 