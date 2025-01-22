package com.erdidev.scheduler.repository;

import com.erdidev.scheduler.enums.ScheduleStatus;
import com.erdidev.scheduler.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTaskId(Long taskId);
    List<Schedule> findByStatusAndScheduledTimeBefore(ScheduleStatus status, LocalDateTime time);
    List<Schedule> findByStatusAndScheduledTimeBetween(ScheduleStatus status, 
            LocalDateTime startTime, LocalDateTime endTime);
} 