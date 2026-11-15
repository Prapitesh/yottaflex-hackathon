package com.hackathon.api.service.impl;

import com.hackathon.api.service.DepartmentService;

import com.hackathon.api.dto.request.DepartmentRequest;
import com.hackathon.api.dto.response.DepartmentResponse;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.Department;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Coordinates department use cases, persistence and audit recording.
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditEventRepository auditEventRepository;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            AuditEventRepository auditEventRepository) {
        this.departmentRepository = departmentRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String normalizedName = request.name().trim();

        if (departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("A department with this name already exists");
        }

        Department department = new Department(
                EntityIdGenerator.departmentId(),
                normalizedName,
                normalizeOptional(request.costCenter())
        );

        Department saved = departmentRepository.save(department);
        recordAudit(saved.getId(), "DEPARTMENT_CREATED", "SYSTEM",
                Map.of("name", saved.getName()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(String departmentId) {
        return toResponse(findDepartment(departmentId));
    }

    @Override
    public DepartmentResponse updateDepartment(
            String departmentId, DepartmentRequest request) {
        Department department = findDepartment(departmentId);
        String normalizedName = request.name().trim();

        departmentRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(departmentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A department with this name already exists");
                });

        department.updateDetails(
                normalizedName,
                normalizeOptional(request.costCenter())
        );

        Department saved = departmentRepository.save(department);
        recordAudit(saved.getId(), "DEPARTMENT_UPDATED", "SYSTEM",
                Map.of("name", saved.getName()));
        return toResponse(saved);
    }

    private Department findDepartment(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + id));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void recordAudit(
            String aggregateId, String eventType, String actor,
            Map<String, Object> payload) {
        auditEventRepository.save(new AuditEvent(
                "DEPARTMENT", aggregateId, eventType, actor, payload));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCostCenter(),
                department.getCreatedAt()
        );
    }
}
