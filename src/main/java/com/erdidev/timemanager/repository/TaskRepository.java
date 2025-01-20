package com.erdidev.timemanager.repository;

import com.erdidev.timemanager.model.Task;
import com.erdidev.timemanager.model.TaskStatus;
import com.erdidev.timemanager.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(Priority priority);
    List<Task> findByDueDateBefore(LocalDateTime date);
    List<Task> findByTitleContainingIgnoreCase(String title);
} 