package com.erdidev.scheduler.controller;

import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminder", description = "Reminder management APIs")
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping
    @Operation(summary = "Create a new reminder")
    public ResponseEntity<ReminderDto> createReminder(@Valid @RequestBody ReminderDto reminderDto) {
        return ResponseEntity.ok(reminderService.createReminder(reminderDto));
    }

    @GetMapping("/due")
    @Operation(summary = "Get due reminders")
    public ResponseEntity<List<ReminderDto>> getDueReminders(
            @RequestParam(defaultValue = "1") long hoursAhead) {
        return ResponseEntity.ok(reminderService.getDueReminders(Duration.ofHours(hoursAhead)));
    }

    @PostMapping("/{id}/trigger")
    @Operation(summary = "Manually trigger a reminder notification")
    @ApiResponse(responseCode = "200", description = "Reminder notification triggered successfully")
    @ApiResponse(responseCode = "404", description = "Reminder not found")
    public ResponseEntity<Void> triggerReminder(@PathVariable Long id) {
        reminderService.processReminder(id);
        return ResponseEntity.ok().build();
    }
} 