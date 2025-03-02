package com.erdidev.timetracker.repository;

import com.erdidev.timetracker.model.TimeEntry;
import com.erdidev.timetracker.model.TimeEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    
    Page<TimeEntry> findByUserId(Long userId, Pageable pageable);
    
    Page<TimeEntry> findByTaskId(Long taskId, Pageable pageable);
    
    Page<TimeEntry> findByUserIdAndStartTimeBetween(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<TimeEntry> findByTaskIdAndStartTimeBetween(
            Long taskId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<TimeEntry> findFirstByUserIdAndStatusOrderByStartTimeDesc(Long userId, TimeEntryStatus status);
    
    // Get total duration for a task
    @Query("SELECT SUM(t.durationSeconds) FROM TimeEntry t WHERE t.task.id = :taskId")
    Long getTotalDurationForTask(@Param("taskId") Long taskId);
    
    // Get total duration for a user in a date range
    @Query("SELECT SUM(t.durationSeconds) FROM TimeEntry t WHERE t.userId = :userId " +
           "AND t.startTime >= :start AND t.startTime <= :end")
    Long getTotalDurationByUserAndDateRange(
            @Param("userId") Long userId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
} 