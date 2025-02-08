package com.erdidev.scheduler.controller;

import com.erdidev.scheduler.dto.RecurrencePatternDto;
import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.scheduler.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "Schedule management APIs")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Create a new schedule")
    public ResponseEntity<ScheduleDto> createSchedule(@Valid @RequestBody ScheduleDto scheduleDto) {
        if (scheduleDto.getEndTime() == null) {
            scheduleDto.setEndTime(scheduleDto.getScheduledTime().plusHours(1));
        }
        
        if (scheduleDto.getStartTime() == null) {
            scheduleDto.setStartTime(scheduleDto.getScheduledTime());
        }
        
        return ResponseEntity.ok(scheduleService.createSchedule(scheduleDto));
    }

    @PostMapping("/tasks/{taskId}")
    @Operation(summary = "Schedule a task")
    public ResponseEntity<ScheduleDto> scheduleTask(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime,
            @RequestBody(required = false) RecurrencePatternDto recurrence) {
        return ResponseEntity.ok(scheduleService.scheduleTask(taskId, scheduledTime, recurrence));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a schedule by ID")
    public ResponseEntity<ScheduleDto> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getSchedule(id));
    }

    @GetMapping
    @Operation(summary = "Get all schedules (paginated)")
    public ResponseEntity<Page<ScheduleDto>> getSchedules(Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getSchedules(pageable));
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get schedules for a specific task")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTaskId(taskId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending schedules")
    public ResponseEntity<List<ScheduleDto>> getPendingSchedules() {
        return ResponseEntity.ok(scheduleService.getPendingSchedules());
    }

    @GetMapping("/due")
    @Operation(summary = "Get schedules due within specified hours")
    public ResponseEntity<List<ScheduleDto>> getDueSchedules(
            @RequestParam(defaultValue = "24") long hoursAhead) {
        return ResponseEntity.ok(scheduleService.getDueSchedules(Duration.ofHours(hoursAhead)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a schedule")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable Long id, 
            @Valid @RequestBody ScheduleDto scheduleDto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDto));
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule a task")
    public ResponseEntity<ScheduleDto> rescheduleTask(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime) {
        return ResponseEntity.ok(scheduleService.rescheduleTask(id, newTime));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update schedule status")
    public ResponseEntity<ScheduleDto> updateStatus(
            @PathVariable Long id,
            @RequestParam ScheduleStatus status) {
        return ResponseEntity.ok(scheduleService.updateScheduleStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a schedule")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
} 