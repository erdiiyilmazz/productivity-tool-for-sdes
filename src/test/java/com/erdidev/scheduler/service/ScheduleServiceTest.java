package com.erdidev.scheduler.service;

import com.erdidev.scheduler.dto.RecurrencePatternDto;
import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.scheduler.exception.ScheduleNotFoundException;
import com.erdidev.scheduler.mapper.ScheduleMapper;
import com.erdidev.scheduler.model.Schedule;
import com.erdidev.scheduler.repository.ScheduleRepository;
import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.model.TaskStatus;
import com.erdidev.taskmanager.repository.TaskRepository;
import com.erdidev.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ReminderService reminderService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule schedule;
    private ScheduleDto scheduleDto;
    private Task task;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.SCHEDULED);
        
        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setTask(task);
        schedule.setScheduledTime(now.plusHours(2));
        schedule.setStartTime(now.plusHours(2));
        schedule.setEndTime(now.plusHours(3));
        schedule.setStatus(ScheduleStatus.PENDING);
        schedule.setTitle("Test Schedule");
        schedule.setDescription("Test Description");
        schedule.setTimeZone("Europe/Istanbul");
        
        scheduleDto = new ScheduleDto();
        scheduleDto.setId(1L);
        scheduleDto.setTaskId(1L);
        scheduleDto.setScheduledTime(now.plusHours(2));
        scheduleDto.setStartTime(now.plusHours(2));
        scheduleDto.setEndTime(now.plusHours(3));
        scheduleDto.setStatus(ScheduleStatus.PENDING);
        scheduleDto.setTitle("Test Schedule");
        scheduleDto.setDescription("Test Description");
        scheduleDto.setTimeZone("Europe/Istanbul");
        scheduleDto.setCreateDefaultReminder(true);
    }

    @Test
    void createSchedule_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(scheduleMapper.toEntity(scheduleDto)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.createSchedule(scheduleDto);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        assertEquals(scheduleDto.getTitle(), result.getTitle());
        verify(scheduleRepository).save(any(Schedule.class));
        
        // Verify reminder creation with default reminder flag
        verify(reminderService).createReminder(any(ReminderDto.class));
    }

    @Test
    void createSchedule_WithoutDefaultReminder_Success() {
        scheduleDto.setCreateDefaultReminder(false);
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(scheduleMapper.toEntity(scheduleDto)).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.createSchedule(scheduleDto);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        verify(scheduleRepository).save(any(Schedule.class));
        
        // Verify reminder is not created
        verify(reminderService, never()).createReminder(any(ReminderDto.class));
    }

    @Test
    void createSchedule_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            scheduleService.createSchedule(scheduleDto);
        });
        
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void getSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.getSchedule(1L);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
    }

    @Test
    void getSchedule_NotFound_ThrowsException() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ScheduleNotFoundException.class, () -> {
            scheduleService.getSchedule(1L);
        });
    }

    @Test
    void getSchedules_Success() {
        Pageable pageable = mock(Pageable.class);
        Page<Schedule> schedulePage = new PageImpl<>(List.of(schedule));
        when(scheduleRepository.findAll(pageable)).thenReturn(schedulePage);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        Page<ScheduleDto> result = scheduleService.getSchedules(pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(scheduleDto.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getSchedulesByTaskId_Success() {
        when(scheduleRepository.findByTaskId(1L)).thenReturn(List.of(schedule));
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        List<ScheduleDto> result = scheduleService.getSchedulesByTaskId(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(scheduleDto.getId(), result.get(0).getId());
    }

    @Test
    void getPendingSchedules_Success() {
        when(scheduleRepository.findByStatusAndScheduledTimeBefore(
                eq(ScheduleStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(List.of(schedule));
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        List<ScheduleDto> result = scheduleService.getPendingSchedules();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(scheduleDto.getId(), result.get(0).getId());
    }

    @Test
    void updateSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.updateSchedule(1L, scheduleDto);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        assertEquals(scheduleDto.getTitle(), result.getTitle());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void updateSchedule_NotFound_ThrowsException() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ScheduleNotFoundException.class, () -> {
            scheduleService.updateSchedule(1L, scheduleDto);
        });
        
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void deleteSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        doNothing().when(scheduleRepository).deleteById(1L);
        
        assertDoesNotThrow(() -> {
            scheduleService.deleteSchedule(1L);
        });
        
        verify(scheduleRepository).deleteById(1L);
    }

    @Test
    void deleteSchedule_NotFound_ThrowsException() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ScheduleNotFoundException.class, () -> {
            scheduleService.deleteSchedule(1L);
        });
        
        verify(scheduleRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateScheduleStatus_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.updateScheduleStatus(1L, ScheduleStatus.COMPLETED);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void scheduleTask_Success() {
        LocalDateTime scheduledTime = now.plusDays(1);
        RecurrencePatternDto recurrence = null;
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(scheduleMapper.toEntity(any(ScheduleDto.class))).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.scheduleTask(1L, scheduledTime, recurrence);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void scheduleTaskWithReminder_Success() {
        LocalDateTime scheduledTime = now.plusDays(1);
        Duration reminderBefore = Duration.ofHours(1);
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(scheduleMapper.toEntity(any(ScheduleDto.class))).thenReturn(schedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.scheduleTaskWithReminder(1L, scheduledTime, reminderBefore);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void getDueSchedules_Success() {
        LocalDateTime endTime = now.plus(Duration.ofHours(24));
        
        when(scheduleRepository.findByStatusAndScheduledTimeBetween(
                eq(ScheduleStatus.PENDING), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(schedule));
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        List<ScheduleDto> result = scheduleService.getDueSchedules(Duration.ofHours(24));
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(scheduleDto.getId(), result.get(0).getId());
    }

    @Test
    void rescheduleTask_Success() {
        LocalDateTime newTime = now.plusDays(2);
        
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
        
        ScheduleDto result = scheduleService.rescheduleTask(1L, newTime);
        
        assertNotNull(result);
        assertEquals(scheduleDto.getId(), result.getId());
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void processSchedules_Success() {
        when(scheduleRepository.findByStatusAndScheduledTimeBefore(
                eq(ScheduleStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(List.of(schedule));
        
        scheduleService.processSchedules();
        
        verify(taskRepository).save(task);
        verify(scheduleRepository).save(schedule);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(ScheduleStatus.COMPLETED, schedule.getStatus());
    }
} 