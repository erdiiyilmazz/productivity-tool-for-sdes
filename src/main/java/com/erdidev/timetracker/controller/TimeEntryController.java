package com.erdidev.timetracker.controller;

import com.erdidev.common.util.SecurityUtils;
import com.erdidev.timetracker.dto.StopTimeEntryRequest;
import com.erdidev.timetracker.dto.TimeEntryDto;
import com.erdidev.timetracker.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/time-entries")
@RequiredArgsConstructor
@Tag(name = "Time Tracking", description = "APIs for tracking time spent on tasks")
public class TimeEntryController {
    
    private final TimeEntryService timeEntryService;
    
    @GetMapping
    @Operation(summary = "Get time entries for current user")
    public ResponseEntity<Page<TimeEntryDto>> getCurrentUserTimeEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(
                timeEntryService.getTimeEntriesByUser(SecurityUtils.getCurrentUserId(), pageRequest));
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get time entries for current user in date range")
    public ResponseEntity<Page<TimeEntryDto>> getTimeEntriesInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(
                timeEntryService.getTimeEntriesByUserAndDateRange(
                        SecurityUtils.getCurrentUserId(), startDate, endDate, pageRequest));
    }
    
    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get time entries for a specific task")
    public ResponseEntity<Page<TimeEntryDto>> getTimeEntriesForTask(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        return ResponseEntity.ok(timeEntryService.getTimeEntriesByTask(taskId, pageRequest));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get time entry by ID")
    public ResponseEntity<TimeEntryDto> getTimeEntry(@PathVariable Long id) {
        return ResponseEntity.ok(timeEntryService.getTimeEntry(id));
    }
    
    @GetMapping("/current")
    @Operation(summary = "Get currently running time entry for current user")
    public ResponseEntity<TimeEntryDto> getCurrentTimeEntry() {
        TimeEntryDto current = timeEntryService.getCurrentlyRunningTimeEntry();
        if (current == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(current);
    }
    
    @PostMapping("/start")
    @Operation(summary = "Start tracking time for a task")
    public ResponseEntity<TimeEntryDto> startTimeTracking(@Valid @RequestBody TimeEntryDto timeEntryDto) {
        return new ResponseEntity<>(timeEntryService.startTracking(timeEntryDto), HttpStatus.CREATED);
    }
    
    @PostMapping("/{id}/stop")
    @Operation(summary = "Stop tracking time for an entry")
    public ResponseEntity<TimeEntryDto> stopTimeTracking(
            @PathVariable Long id, 
            @RequestBody(required = false) StopTimeEntryRequest request) {
        
        if (request == null) {
            request = new StopTimeEntryRequest();
        }
        
        return ResponseEntity.ok(timeEntryService.stopTracking(id, request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a time entry")
    public ResponseEntity<TimeEntryDto> updateTimeEntry(
            @PathVariable Long id, 
            @Valid @RequestBody TimeEntryDto timeEntryDto) {
        
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(id, timeEntryDto));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a time entry")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/tasks/{taskId}/total-duration")
    @Operation(summary = "Get total duration for a task")
    public ResponseEntity<Long> getTotalDurationForTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(timeEntryService.getTotalDurationForTask(taskId));
    }
    
    @GetMapping("/users/{userId}/total-duration")
    @Operation(summary = "Get total duration for a user in date range")
    public ResponseEntity<Long> getTotalDurationForUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(
                timeEntryService.getTotalDurationForUserInDateRange(userId, startDate, endDate));
    }
} 