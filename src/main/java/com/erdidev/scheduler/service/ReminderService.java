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

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;
    private final NotificationStrategy notificationStrategy;
    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public ReminderDto createReminder(ReminderDto reminderDto) {
        log.debug("Creating reminder for schedule: {}", reminderDto.getScheduleId());
        
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
        return reminderMapper.toDto(savedReminder);
    }

    @Transactional
    public ReminderDto updateReminder(Long id, ReminderDto reminderDto) {
        log.debug("Updating reminder: {}", id);
        
        Reminder existingReminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ReminderNotFoundException(id));
        
        existingReminder.setReminderTime(reminderDto.getReminderTime());
        
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
    public void processReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFoundException(reminderId));
        
        try {
            String message = String.format("Reminder for Schedule #%d at %s",
                reminder.getScheduleId(),
                reminder.getReminderTime());
            
            notificationStrategy.sendNotification(message);
            
            reminder.setStatus(ReminderStatus.SENT);
            reminderRepository.save(reminder);
            
        } catch (Exception e) {
            log.error("Failed to process reminder: {}", reminderId, e);
            reminder.setStatus(ReminderStatus.FAILED);
            reminderRepository.save(reminder);
            throw e;
        }
    }
} 