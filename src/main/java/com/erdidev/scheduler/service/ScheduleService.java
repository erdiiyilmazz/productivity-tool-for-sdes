package com.erdidev.scheduler.service;

import com.erdidev.scheduler.dto.RecurrencePatternDto;
import com.erdidev.scheduler.dto.ScheduleDto;
import com.erdidev.scheduler.enums.RecurrenceType;
import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.scheduler.model.Schedule;
import com.erdidev.scheduler.repository.ScheduleRepository;
import com.erdidev.scheduler.exception.ScheduleNotFoundException;
import com.erdidev.scheduler.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        log.debug("Creating schedule for task: {}", scheduleDto.getTaskId());
        
        Schedule schedule = scheduleMapper.toEntity(scheduleDto);
        schedule.setStatus(ScheduleStatus.PENDING);
        
        if (schedule.getTimeZone() == null) {
            schedule.setTimeZone(ZoneId.systemDefault().getId());
        }
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
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
        existingSchedule.setRecurrencePattern(
            scheduleMapper.toEntity(scheduleDto).getRecurrencePattern()
        );
        existingSchedule.setTimeZone(scheduleDto.getTimeZone());
        
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return scheduleMapper.toDto(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        log.debug("Deleting schedule: {}", id);
        if (!scheduleRepository.existsById(id)) {
            throw new ScheduleNotFoundException(id);
        }
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
        scheduleDto.setRecurrencePattern(recurrence);
        scheduleDto.setStatus(ScheduleStatus.PENDING);
        
        return createSchedule(scheduleDto);
    }

    @Transactional
    public ScheduleDto scheduleRecurringTask(Long taskId, RecurrencePatternDto recurrence) {
        validateRecurrencePattern(recurrence);
        
        LocalDateTime nextOccurrence = calculateNextOccurrence(recurrence);
        return scheduleTask(taskId, nextOccurrence, recurrence);
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
} 