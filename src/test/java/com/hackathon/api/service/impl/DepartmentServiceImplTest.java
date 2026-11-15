package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.DepartmentRequest;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.Department;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepartmentServiceImplTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private DepartmentServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DepartmentServiceImpl(departmentRepository, auditEventRepository);
    }

    @Test
    void createDepartmentNormalizesNameAndRecordsAudit() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createDepartment(
                new DepartmentRequest("  Engineering  ", " ENG-01 "));

        assertEquals("Engineering", response.name());
        assertEquals("ENG-01", response.costCenter());
        verify(departmentRepository).save(any(Department.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void createDepartmentRejectsDuplicateName() {
        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.createDepartment(
                        new DepartmentRequest("Engineering", "ENG-01")));

        verify(departmentRepository, never()).save(any(Department.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    void getDepartmentByIdRejectsUnknownId() {
        when(departmentRepository.findById("DEP-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getDepartmentById("DEP-404"));
    }
}
