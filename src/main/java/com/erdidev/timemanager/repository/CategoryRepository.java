package com.erdidev.timemanager.repository;

import com.erdidev.timemanager.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    List<Category> findByProjectId(Long projectId);
    Page<Category> findByProjectId(Long projectId, Pageable pageable);
    List<Category> findByNameContainingIgnoreCase(String name);
} 