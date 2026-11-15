package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.requisition.RequisitionDecisionRequest;
import com.hackathon.api.dto.request.requisition.RequisitionRequest;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobRequisitionServiceImplTest {

    @Mock private JobRequisitionRepository requisitionRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private AuditEventRepository auditEventRepository;
    @Mock private DataIssueRepository dataIssueRepository;

    private JobRequisitionServiceImpl service;
    private Department department;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new JobRequisitionServiceImpl(
                requisitionRepository, departmentRepository, applicationRepository,
                auditEventRepository, dataIssueRepository);
        department = new Department("DEP-000001", "Engineering", "ENG-01");
    }

    @Test
    void createRequisitionStartsAsDraftAndRecordsAudit() {
        when(departmentRepository.findById("DEP-000001"))
                .thenReturn(Optional.of(department));
        when(requisitionRepository.save(any(JobRequisition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createRequisition(validRequest());

        assertEquals(RequisitionStatus.DRAFT, response.status());
        assertEquals("Software Engineer", response.title());
        verify(requisitionRepository).save(any(JobRequisition.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void requesterCannotApproveOwnRequisition() {
        JobRequisition requisition = pendingRequisition("requester@example.test");
        when(requisitionRepository.findById("REQ-2026-000001"))
                .thenReturn(Optional.of(requisition));

        assertThrows(IllegalStateException.class,
                () -> service.approve("REQ-2026-000001",
                        new RequisitionDecisionRequest(
                                "requester@example.test", "I approve this")));

        assertEquals(RequisitionStatus.PENDING_APPROVAL, requisition.getStatus());
        verify(requisitionRepository, never()).save(any(JobRequisition.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    void anotherActorCanApprovePendingRequisition() {
        JobRequisition requisition = pendingRequisition("requester@example.test");
        when(requisitionRepository.findById("REQ-2026-000001"))
                .thenReturn(Optional.of(requisition));
        when(requisitionRepository.save(any(JobRequisition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.approve("REQ-2026-000001",
                new RequisitionDecisionRequest(
                        "approver@example.test", "Approved headcount"));

        assertEquals(RequisitionStatus.APPROVED, response.status());
        assertEquals("approver@example.test", response.approvedBy());
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    private JobRequisition pendingRequisition(String requester) {
        JobRequisition requisition = new JobRequisition(
                "REQ-2026-000001", department, "Software Engineer",
                "Build services", EmploymentType.FULL_TIME, "Hyderabad", 2,
                new BigDecimal("600000"), new BigDecimal("1200000"),
                "INR", requester);
        requisition.submitForApproval();
        return requisition;
    }

    private RequisitionRequest validRequest() {
        return new RequisitionRequest(
                "DEP-000001", "Software Engineer", "Build services",
                EmploymentType.FULL_TIME, "Hyderabad", 2,
                new BigDecimal("600000"), new BigDecimal("1200000"),
                "INR", "requester@example.test");
    }
}
