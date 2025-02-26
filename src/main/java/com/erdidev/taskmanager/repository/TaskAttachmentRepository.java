package com.erdidev.taskmanager.repository;

import com.erdidev.taskmanager.model.TaskAttachment;
import com.erdidev.taskmanager.model.AttachmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);
    List<TaskAttachment> findByTaskIdAndType(Long taskId, AttachmentType type);
    void deleteByTaskId(Long taskId);
} 