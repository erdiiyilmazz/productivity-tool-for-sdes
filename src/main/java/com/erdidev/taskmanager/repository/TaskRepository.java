package com.erdidev.taskmanager.repository;

import com.erdidev.taskmanager.model.Task;
import com.erdidev.taskmanager.model.TaskStatus;
import com.erdidev.taskmanager.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    List<Task> findByTitleContainingIgnoreCase(String title);
    Page<Task> findByPriority(Priority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :date")
    Page<Task> findOverdueTasks(@Param("date") LocalDateTime date, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Task> searchTasks(@Param("query") String query, Pageable pageable);
} 