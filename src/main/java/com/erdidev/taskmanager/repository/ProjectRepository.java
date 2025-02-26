package com.erdidev.taskmanager.repository;

import com.erdidev.taskmanager.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByName(String name);
    List<Project> findByNameContainingIgnoreCase(String name);
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);
} 