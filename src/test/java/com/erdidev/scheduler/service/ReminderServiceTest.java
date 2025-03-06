package com.erdidev.scheduler.service;

import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.enums.NotificationChannel;
import com.erdidev.scheduler.enums.ReminderStatus;
import com.erdidev.scheduler.exception.NotificationDeliveryException;
import com.erdidev.scheduler.exception.ReminderNotFoundException;
import com.erdidev.scheduler.mapper.ReminderMapper;
import com.erdidev.scheduler.model.Reminder;
import com.erdidev.scheduler.model.ReminderNotificationChannel;
import com.erdidev.scheduler.model.Schedule;
import com.erdidev.scheduler.repository.ReminderRepository;
import com.erdidev.scheduler.repository.ScheduleRepository;
import com.erdidev.scheduler.service.notification.NotificationStrategy;
import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReminderMapper reminderMapper;

    @Mock
    private NotificationStrategy notificationStrategy;

    @InjectMocks
    private ReminderService reminderService;

    private ReminderDto reminderDto;
    private Reminder reminder;
    private Task task;
    private Schedule schedule;
    private ZonedDateTime now;
    private LocalDateTime nowLocal;

    @BeforeEach
    void setUp() {
        // Set up current time
        now = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
        nowLocal = now.toLocalDateTime();
        
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDueDate(nowLocal.plusDays(2));
        
        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setTask(task);
        schedule.setScheduledTime(nowLocal.plusDays(1));
        
        reminder = new Reminder();
        reminder.setId(1L);
        reminder.setReminderTime(nowLocal.plusHours(1));
        reminder.setMessage("Test Reminder");
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setTask(task);
        
        Set<ReminderNotificationChannel> notificationChannels = new HashSet<>();
        ReminderNotificationChannel channel = new ReminderNotificationChannel();
        channel.setChannelType(NotificationChannel.WEBSOCKET);
        notificationChannels.add(channel);
        reminder.setNotificationChannels(notificationChannels);
        
        reminderDto = new ReminderDto();
        reminderDto.setId(1L);
        reminderDto.setReminderTime(now.plusHours(1));
        reminderDto.setMessage("Test Reminder");
        reminderDto.setStatus(ReminderStatus.PENDING);
        reminderDto.setTaskId(1L);
        reminderDto.setScheduleId(1L);
        reminderDto.setNotificationChannels(Set.of(NotificationChannel.WEBSOCKET));

        // Set up default mocks
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
    }

    @Test
    void createReminder_Success() {
        when(reminderMapper.toEntity(any(ReminderDto.class))).thenReturn(reminder);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderDto);
        
        ReminderDto result = reminderService.createReminder(reminderDto);
        
        assertNotNull(result);
        assertEquals(reminderDto.getId(), result.getId());
        assertEquals(reminderDto.getMessage(), result.getMessage());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    void createReminder_PastTime_ThrowsException() {
        ReminderDto pastTimeDto = new ReminderDto();
        pastTimeDto.setScheduleId(1L);
        pastTimeDto.setTaskId(1L);
        pastTimeDto.setReminderTime(now.minusHours(1)); // Past time
        pastTimeDto.setNotificationChannels(Set.of(NotificationChannel.WEBSOCKET));
        
        assertThrows(IllegalArgumentException.class, () -> {
            reminderService.createReminder(pastTimeDto);
        });
        
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void createReminder_AfterScheduleTime_ThrowsException() {
        ReminderDto afterScheduleDto = new ReminderDto();
        afterScheduleDto.setScheduleId(1L);
        afterScheduleDto.setTaskId(1L);
        afterScheduleDto.setReminderTime(now.plusDays(2)); // After schedule time
        afterScheduleDto.setNotificationChannels(Set.of(NotificationChannel.WEBSOCKET));
        
        assertThrows(IllegalArgumentException.class, () -> {
            reminderService.createReminder(afterScheduleDto);
        });
        
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void updateReminder_Success() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);
        when(reminderMapper.toDto(reminder)).thenReturn(reminderDto);
        
        ReminderDto result = reminderService.updateReminder(1L, reminderDto);
        
        assertNotNull(result);
        assertEquals(reminderDto.getId(), result.getId());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    void updateReminder_NotFound_ThrowsException() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ReminderNotFoundException.class, () -> {
            reminderService.updateReminder(1L, reminderDto);
        });
        
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void getDueReminders_Success() {
        when(reminderRepository.findByStatusAndReminderTimeBetween(
                eq(ReminderStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(reminder));
        when(reminderMapper.toDto(any(Reminder.class))).thenReturn(reminderDto);
        
        List<ReminderDto> result = reminderService.getDueReminders(Duration.ofHours(1));
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reminderDto.getId(), result.get(0).getId());
    }

    @Test
    void processReminder_Success() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));
        doNothing().when(notificationStrategy).sendNotification(anyString());
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);
        
        assertDoesNotThrow(() -> reminderService.processReminder(1L));
        
        verify(notificationStrategy).sendNotification(anyString());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    void processReminder_NotFound_ThrowsException() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ReminderNotFoundException.class, () -> {
            reminderService.processReminder(1L);
        });
        
        verify(notificationStrategy, never()).sendNotification(anyString());
    }

    @Test
    void processReminder_NotificationFails_ThrowsException() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));
        doThrow(new RuntimeException("Failed to send notification"))
            .when(notificationStrategy).sendNotification(anyString());
        
        assertThrows(NotificationDeliveryException.class, () -> {
            reminderService.processReminder(1L);
        });
        
        verify(reminderRepository, never()).save(any(Reminder.class));
    }
} 