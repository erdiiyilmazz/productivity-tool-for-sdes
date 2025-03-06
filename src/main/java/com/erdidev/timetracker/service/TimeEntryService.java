package com.erdidev.timetracker.service;

import com.erdidev.common.util.SecurityUtils;
import com.erdidev.taskmanager.exception.TaskNotFoundException;
import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.repository.TaskRepository;
import com.erdidev.timetracker.dto.StopTimeEntryRequest;
import com.erdidev.timetracker.dto.TimeEntryDto;
import com.erdidev.timetracker.exception.TimeEntryNotFoundException;
import com.erdidev.timetracker.exception.TimeTrackingException;
import com.erdidev.timetracker.mapper.TimeEntryMapper;
import com.erdidev.timetracker.model.TimeEntry;
import com.erdidev.timetracker.model.TimeEntryStatus;
import com.erdidev.timetracker.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeEntryService {
    
    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;
    
    @Transactional(readOnly = true)
    public Page<TimeEntryDto> getTimeEntriesByUser(Long userId, Pageable pageable) {
        return timeEntryRepository.findByUserId(userId, pageable)
                .map(timeEntryMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TimeEntryDto> getTimeEntriesByTask(Long taskId, Pageable pageable) {
        return timeEntryRepository.findByTaskId(taskId, pageable)
                .map(timeEntryMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TimeEntryDto> getTimeEntriesByUserAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        return timeEntryRepository.findByUserIdAndStartTimeBetween(userId, start, end, pageable)
                .map(timeEntryMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public TimeEntryDto getTimeEntry(Long id) {
        return timeEntryRepository.findById(id)
                .map(timeEntryMapper::toDto)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
    }
    
    @Transactional
    public TimeEntryDto startTracking(TimeEntryDto timeEntryDto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // Check if user already has a running time entry
        Optional<TimeEntry> runningEntry = timeEntryRepository
                .findFirstByUserIdAndStatusOrderByStartTimeDesc(currentUserId, TimeEntryStatus.RUNNING);
        
        if (runningEntry.isPresent()) {
            throw new TimeTrackingException("You already have a running time entry. Please stop it first.");
        }
        
        // Validate task exists
        Task task = taskRepository.findById(timeEntryDto.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(timeEntryDto.getTaskId()));
        
        // Create new time entry
        TimeEntry timeEntry = timeEntryMapper.toEntity(timeEntryDto);
        timeEntry.setTask(task);
        timeEntry.setUserId(currentUserId);
        timeEntry.setStatus(TimeEntryStatus.RUNNING);
        
        // If start time is null, set it to now
        if (timeEntry.getStartTime() == null) {
            timeEntry.setStartTime(LocalDateTime.now());
        }
        
        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        log.debug("Started time tracking for task: {}", timeEntryDto.getTaskId());
        
        return timeEntryMapper.toDto(savedEntry);
    }
    
    @Transactional
    public TimeEntryDto stopTracking(Long timeEntryId, StopTimeEntryRequest request) {
        TimeEntry timeEntry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new TimeEntryNotFoundException(timeEntryId));
        
        // Verify ownership
        if (!timeEntry.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new TimeTrackingException("You can only stop your own time entries");
        }
        
        // Verify the entry is running
        if (timeEntry.getStatus() != TimeEntryStatus.RUNNING) {
            throw new TimeTrackingException("This time entry is not running");
        }
        
        // Set description and end time
        if (request.getDescription() != null) {
            timeEntry.setDescription(request.getDescription());
        }
        
        timeEntry.setEndTime(LocalDateTime.now());
        timeEntry.setStatus(TimeEntryStatus.COMPLETED);
        
        TimeEntry updatedEntry = timeEntryRepository.save(timeEntry);
        log.debug("Stopped time tracking for entry: {}", timeEntryId);
        
        return timeEntryMapper.toDto(updatedEntry);
    }
    
    @Transactional
    public TimeEntryDto updateTimeEntry(Long id, TimeEntryDto timeEntryDto) {
        TimeEntry existingEntry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        
        // Verify ownership
        if (!existingEntry.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new TimeTrackingException("You can only update your own time entries");
        }
        
        // Update fields
        timeEntryMapper.updateEntity(timeEntryDto, existingEntry);
        
        // If task is changing, validate new task exists
        if (timeEntryDto.getTaskId() != null && 
            !timeEntryDto.getTaskId().equals(existingEntry.getTask().getId())) {
            
            Task task = taskRepository.findById(timeEntryDto.getTaskId())
                    .orElseThrow(() -> new TaskNotFoundException(timeEntryDto.getTaskId()));
            existingEntry.setTask(task);
        }
        
        TimeEntry updatedEntry = timeEntryRepository.save(existingEntry);
        log.debug("Updated time entry: {}", id);
        
        return timeEntryMapper.toDto(updatedEntry);
    }
    
    @Transactional
    public void deleteTimeEntry(Long id) {
        TimeEntry timeEntry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        
        // Verify ownership
        if (!timeEntry.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new TimeTrackingException("You can only delete your own time entries");
        }
        
        timeEntryRepository.delete(timeEntry);
        log.debug("Deleted time entry: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Long getTotalDurationForTask(Long taskId) {
        return timeEntryRepository.getTotalDurationForTask(taskId);
    }
    
    @Transactional(readOnly = true)
    public Long getTotalDurationForUserInDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        return timeEntryRepository.getTotalDurationByUserAndDateRange(userId, start, end);
    }
    
    @Transactional(readOnly = true)
    public TimeEntryDto getCurrentlyRunningTimeEntry() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        return timeEntryRepository
                .findFirstByUserIdAndStatusOrderByStartTimeDesc(currentUserId, TimeEntryStatus.RUNNING)
                .map(timeEntryMapper::toDto)
                .orElse(null);
    }
} 