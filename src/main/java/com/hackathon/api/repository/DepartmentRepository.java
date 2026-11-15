package com.hackathon.api.repository;

import com.hackathon.api.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for the Department master entity.
 * Spring Data generates the implementation at runtime.
 */
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
