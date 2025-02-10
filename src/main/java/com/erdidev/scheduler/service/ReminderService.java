package com.erdidev.scheduler.service;

import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.mapper.ReminderMapper;
import com.erdidev.scheduler.model.Reminder;
import com.erdidev.scheduler.exception.ReminderNotFoundException;
import com.erdidev.scheduler.repository.ReminderRepository;
import com.erdidev.scheduler.enums.ReminderStatus;
import com.erdidev.timemanager.model.Task;
import com.erdidev.timemanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.erdidev.scheduler.service.notification.NotificationStrategy;
import com.erdidev.scheduler.model.ReminderNotificationChannel;
import com.erdidev.scheduler.model.Schedule;
import com.erdidev.scheduler.repository.ScheduleRepository;
import com.erdidev.scheduler.exception.NotificationDeliveryException;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReminderMapper reminderMapper;
    private final NotificationStrategy notificationStrategy;
    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public ReminderDto createReminder(ReminderDto reminderDto) {
        log.debug("Creating reminder for schedule: {}", reminderDto.getScheduleId());
        
        // Validate schedule exists
        Schedule schedule = scheduleRepository.findById(reminderDto.getScheduleId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Schedule not found with id: " + reminderDto.getScheduleId()));
        
        // Time validations
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));  // Use Istanbul timezone
        
        // 1. Reminder time should be in the future
        if (reminderDto.getReminderTime().isBefore(now)) {
            throw new IllegalArgumentException("Reminder time must be in the future. " +
                "Current time: " + now + ", Reminder time: " + reminderDto.getReminderTime());
        }
        
        // Convert reminder time to LocalDateTime for storage
        LocalDateTime reminderLocalTime = reminderDto.getReminderTime().toLocalDateTime();
        
        // 2. Reminder time should be before schedule time
        if (reminderLocalTime.isAfter(schedule.getScheduledTime())) {
            throw new IllegalArgumentException(
                "Reminder time must be before schedule time: " + schedule.getScheduledTime());
        }
        
        // 3. If task has due date, reminder should be before that
        if (schedule.getTask().getDueDate() != null && 
            reminderLocalTime.isAfter(schedule.getTask().getDueDate())) {
            throw new IllegalArgumentException(
                "Reminder time cannot be after task due date: " + schedule.getTask().getDueDate());
        }
        
        // 4. Validate minimum time gap (e.g., at least 1 minute before schedule)
        Duration timeUntilSchedule = Duration.between(
            reminderLocalTime, 
            schedule.getScheduledTime()
        );
        if (timeUntilSchedule.toMinutes() < 1) {
            throw new IllegalArgumentException(
                "Reminder must be set at least 1 minute before schedule time");
        }
        
        // Set default values
        if (reminderDto.getStatus() == null) {
            reminderDto.setStatus(ReminderStatus.PENDING);
        }
        
        Reminder reminder = reminderMapper.toEntity(reminderDto);
        
        // Convert NotificationChannel to ReminderNotificationChannel
        Set<ReminderNotificationChannel> channels = reminderDto.getNotificationChannels().stream()
            .map(channel -> {
                ReminderNotificationChannel notificationChannel = new ReminderNotificationChannel();
                notificationChannel.setNotificationChannel(channel);
                notificationChannel.setChannelType(channel);
                return notificationChannel;
            })
            .collect(Collectors.toSet());
        
        reminder.setNotificationChannels(channels);
        reminder.setType(reminderDto.getType());
        reminder.setStatus(ReminderStatus.PENDING);
        
        if (reminderDto.getTaskId() != null) {
            Task task = taskRepository.findById(reminderDto.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
            reminder.setTask(task);
        } else {
            throw new IllegalArgumentException("Task ID is required");
        }
        
        Reminder savedReminder = reminderRepository.save(reminder);
        
        log.info("Created reminder {} for schedule {} at time {}", 
            savedReminder.getId(), 
            schedule.getId(), 
            reminderDto.getReminderTime());
            
        return reminderMapper.toDto(savedReminder);
    }

    @Transactional
    public ReminderDto updateReminder(Long id, ReminderDto reminderDto) {
        log.debug("Updating reminder: {}", id);
        
        Reminder existingReminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ReminderNotFoundException(id));
        
        existingReminder.setReminderTime(reminderDto.getReminderTime().toLocalDateTime());
        
        Reminder updatedReminder = reminderRepository.save(existingReminder);
        return reminderMapper.toDto(updatedReminder);
    }

    @Transactional(readOnly = true)
    public List<ReminderDto> getDueReminders(Duration lookAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plus(lookAhead);
        
        return reminderRepository.findByStatusAndReminderTimeBetween(
                ReminderStatus.PENDING, now, endTime)
            .stream()
            .map(reminderMapper::toDto)
            .toList();
    }

    @Transactional
    public void processReminder(Long id) {
        log.info("Processing reminder: {}", id);
        
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ReminderNotFoundException(id));
            
        try {
            log.debug("Sending notification for reminder: {}", id);
            notificationStrategy.sendNotification(reminder.getMessage());
            
            // Update reminder status
            reminderRepository.save(reminder);
            
            log.info("Successfully processed reminder: {}", id);
        } catch (Exception e) {
            log.error("Failed to process reminder: {}", id, e);
            throw new NotificationDeliveryException("Failed to process reminder: " + id, e);
        }
    }
} 