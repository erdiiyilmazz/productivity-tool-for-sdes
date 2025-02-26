package com.erdidev.scheduler.service;

import com.erdidev.scheduler.dto.RecurrencePatternDto;
import com.erdidev.scheduler.dto.ReminderDto;
import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.enums.RecurrenceType;
import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.scheduler.model.Schedule;
import com.erdidev.scheduler.repository.ScheduleRepository;
import com.erdidev.scheduler.exception.ScheduleNotFoundException;
import com.erdidev.scheduler.mapper.ScheduleMapper;
import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.model.TaskStatus;
import com.erdidev.taskmanager.repository.TaskRepository;
import com.erdidev.taskmanager.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TaskRepository taskRepository;
    private final ScheduleMapper scheduleMapper;
    private final ReminderService reminderService;
    private final TaskService taskService;

    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        log.debug("Creating schedule for task: {}", scheduleDto.getTaskId());
        
        // Get the task
        Task task = taskRepository.findById(scheduleDto.getTaskId())
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        LocalDateTime now = LocalDateTime.now();
        
        // Time validations
        // 1. Schedule time should be in the future
        if (scheduleDto.getScheduledTime().isBefore(now)) {
            throw new IllegalArgumentException("Schedule time must be in the future");
        }
        
        // 2. Validate against task due date
        if (task.getDueDate() != null && 
            scheduleDto.getScheduledTime().isAfter(task.getDueDate())) {
            throw new IllegalArgumentException(
                "Schedule time cannot be after task due date: " + task.getDueDate());
        }
        
        // 3. Validate start and end times
        if (scheduleDto.getStartTime() != null && 
            scheduleDto.getStartTime().isAfter(scheduleDto.getScheduledTime())) {
            throw new IllegalArgumentException("Start time cannot be after scheduled time");
        }
        
        // Set default times
        if (scheduleDto.getStartTime() == null) {
            scheduleDto.setStartTime(scheduleDto.getScheduledTime());
        }
        
        if (scheduleDto.getEndTime() == null) {
            scheduleDto.setEndTime(scheduleDto.getScheduledTime().plusHours(1));
        }
        
        // 4. Validate end time
        if (scheduleDto.getEndTime().isBefore(scheduleDto.getStartTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        
        Schedule schedule = scheduleMapper.toEntity(scheduleDto);
        schedule.setTask(task);
        
        // Set defaults
        if (schedule.getStatus() == null) {
            schedule.setStatus(ScheduleStatus.PENDING);
        }
        
        if (schedule.getTimeZone() == null) {
            schedule.setTimeZone(ZoneId.systemDefault().getId());
        }
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Created schedule {} for task {} at time {}", 
            savedSchedule.getId(), 
            task.getId(), 
            scheduleDto.getScheduledTime());
        
        // Update task status when schedule is created
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.SCHEDULED);
            taskRepository.save(task);
        }

        // Create default reminder if not specified
        if (scheduleDto.getCreateDefaultReminder()) {
            ReminderDto reminderDto = new ReminderDto();
            reminderDto.setReminderTime(
                scheduleDto.getScheduledTime()
                    .minusMinutes(15)
                    .atZone(ZoneId.systemDefault())  // Convert to ZonedDateTime
            );
            reminderDto.setMessage("Task scheduled to start in 15 minutes: " + task.getTitle());
            reminderService.createReminder(reminderDto);
        }

        return scheduleMapper.toDto(savedSchedule);
    }

    @Transactional(readOnly = true)
    public ScheduleDto getSchedule(Long id) {
        log.debug("Fetching schedule: {}", id);
        return scheduleRepository.findById(id)
                .map(scheduleMapper::toDto)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ScheduleDto> getSchedules(Pageable pageable) {
        return scheduleRepository.findAll(pageable)
                .map(scheduleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByTaskId(Long taskId) {
        return scheduleRepository.findByTaskId(taskId).stream()
                .map(scheduleMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> getPendingSchedules() {
        return scheduleRepository.findByStatusAndScheduledTimeBefore(
                ScheduleStatus.PENDING, 
                LocalDateTime.now()
            ).stream()
            .map(scheduleMapper::toDto)
            .toList();
    }

    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        log.debug("Updating schedule: {}", id);
        
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
        
        existingSchedule.setScheduledTime(scheduleDto.getScheduledTime());
        existingSchedule.setStatus(scheduleDto.getStatus());
        existingSchedule.setTimeZone(scheduleDto.getTimeZone());
        existingSchedule.setTitle(scheduleDto.getTitle());
        existingSchedule.setDescription(scheduleDto.getDescription());
        
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return scheduleMapper.toDto(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        log.debug("Deleting schedule: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
        
        scheduleRepository.deleteById(id);
    }

    @Transactional
    public ScheduleDto updateScheduleStatus(Long id, ScheduleStatus status) {
        log.debug("Updating schedule status: {} to {}", id, status);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
        
        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(updatedSchedule);
    }

    @Transactional
    public ScheduleDto scheduleTask(Long taskId, LocalDateTime scheduledTime, RecurrencePatternDto recurrence) {
        log.debug("Scheduling task {} for {}", taskId, scheduledTime);
        
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTaskId(taskId);
        scheduleDto.setScheduledTime(scheduledTime);
        scheduleDto.setStatus(ScheduleStatus.PENDING);
        
        return createSchedule(scheduleDto);
    }

    @Transactional
    public ScheduleDto scheduleTaskWithReminder(Long taskId, LocalDateTime scheduledTime, 
            Duration reminderBefore) {
        ScheduleDto schedule = scheduleTask(taskId, scheduledTime, null);
        
        // reminder logic here
        LocalDateTime reminderTime = scheduledTime.minus(reminderBefore);
        // reminderService.createReminder(schedule.getId(), reminderTime);
        
        return schedule;
    }

    @Transactional
    public List<ScheduleDto> getDueSchedules(Duration lookAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plus(lookAhead);
        
        return scheduleRepository.findByStatusAndScheduledTimeBetween(
                ScheduleStatus.PENDING, now, endTime)
            .stream()
            .map(scheduleMapper::toDto)
            .toList();
    }

    @Transactional
    public ScheduleDto rescheduleTask(Long scheduleId, LocalDateTime newTime) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        
        schedule.setScheduledTime(newTime);
        schedule.setStatus(ScheduleStatus.PENDING);
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    private void validateRecurrencePattern(RecurrencePatternDto recurrence) {
        if (recurrence.getType() == null) {
            throw new IllegalArgumentException("Recurrence type is required");
        }

        switch (recurrence.getType()) {
            case RecurrenceType.WEEKLY:
                if (recurrence.getDaysOfWeek() == null || recurrence.getDaysOfWeek().isEmpty()) {
                    throw new IllegalArgumentException("Weekly recurrence requires days of week");
                }
                break;
            case RecurrenceType.MONTHLY:
                if (recurrence.getDayOfMonth() == null || 
                    recurrence.getDayOfMonth() < 1 || 
                    recurrence.getDayOfMonth() > 31) {
                    throw new IllegalArgumentException("Monthly recurrence requires valid day of month");
                }
                break;
            // Other validations
        }
    }

    private LocalDateTime calculateNextOccurrence(RecurrencePatternDto recurrence) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (recurrence.getType()) {
            case RecurrenceType.DAILY -> now.plusDays(1).withHour(9).withMinute(0);
            case RecurrenceType.WEEKLY -> calculateNextWeeklyOccurrence(now, recurrence.getDaysOfWeek());
            case RecurrenceType.MONTHLY -> calculateNextMonthlyOccurrence(now, recurrence.getDayOfMonth());
            case RecurrenceType.YEARLY -> now.plusYears(1);
        };
    }

    private LocalDateTime calculateNextWeeklyOccurrence(LocalDateTime now, 
            Set<DayOfWeek> daysOfWeek) {
        return daysOfWeek.stream()
                .map(day -> now.with(TemporalAdjusters.nextOrSame(day)))
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No valid day of week found"));
    }

    private LocalDateTime calculateNextMonthlyOccurrence(LocalDateTime now, Integer dayOfMonth) {
        LocalDateTime candidate = now.withDayOfMonth(dayOfMonth);
        return candidate.isBefore(now) ? 
                candidate.plusMonths(1) : candidate;
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> dueSchedules = scheduleRepository
            .findByStatusAndScheduledTimeBefore(ScheduleStatus.PENDING, now);

        for (Schedule schedule : dueSchedules) {
            try {
                // Update task status when schedule time arrives
                Task task = schedule.getTask();
                if (task.getStatus() == TaskStatus.SCHEDULED) {
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    taskRepository.save(task);
                }

                // Mark schedule as completed
                schedule.setStatus(ScheduleStatus.COMPLETED);
                scheduleRepository.save(schedule);

                log.info("Processed schedule {} for task {}", 
                    schedule.getId(), task.getId());
            } catch (Exception e) {
                log.error("Error processing schedule {}", schedule.getId(), e);
            }
        }
    }
} 