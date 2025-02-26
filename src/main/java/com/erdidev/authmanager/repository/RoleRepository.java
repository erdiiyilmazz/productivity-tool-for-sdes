package com.erdidev.authmanager.repository;

import com.erdidev.authmanager.model.Role;
import com.erdidev.authmanager.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
} 