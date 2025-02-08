package com.erdidev.scheduler.repository;

import com.erdidev.scheduler.model.Reminder;
import com.erdidev.scheduler.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByScheduleId(Long scheduleId);
    List<Reminder> findByStatusAndReminderTimeBefore(ReminderStatus status, LocalDateTime time);
    List<Reminder> findByStatusAndReminderTimeBetween(ReminderStatus status, 
            LocalDateTime startTime, LocalDateTime endTime);
} 