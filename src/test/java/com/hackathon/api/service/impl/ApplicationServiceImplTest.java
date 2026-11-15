package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.application.ApplicationRequest;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationServiceImplTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobRequisitionRepository requisitionRepository;
    @Mock private CandidateRepository candidateRepository;
    @Mock private ResumeArtifactRepository resumeArtifactRepository;
    @Mock private ApplicationStageHistoryRepository stageHistoryRepository;
    @Mock private AuditEventRepository auditEventRepository;
    @Mock private DataIssueRepository dataIssueRepository;
    @Mock private OfferRepository offerRepository;

    private ApplicationServiceImpl service;
    private JobRequisition approvedRequisition;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ApplicationServiceImpl(
                applicationRepository, requisitionRepository, candidateRepository,
                resumeArtifactRepository, stageHistoryRepository,
                auditEventRepository, dataIssueRepository, offerRepository);

        Department department = new Department("DEP-000001", "Engineering", "ENG-01");
        approvedRequisition = new JobRequisition(
                "REQ-2026-000001", department, "Software Engineer",
                "Build services", EmploymentType.FULL_TIME, "Hyderabad", 2,
                new BigDecimal("600000"), new BigDecimal("1200000"),
                "INR", "requester@example.test");
        approvedRequisition.approve("approver@example.test", OffsetDateTime.now());

        candidate = new Candidate("CAND-000001", "Asha Rao",
                "asha@example.test", null, "Hyderabad", 18);
    }

    @Test
    void createApplicationPersistsStageHistoryAndAudit() {
        when(requisitionRepository.findById("REQ-2026-000001"))
                .thenReturn(Optional.of(approvedRequisition));
        when(candidateRepository.findById("CAND-000001"))
                .thenReturn(Optional.of(candidate));
        when(applicationRepository.findByRequisition_IdAndCandidate_Id(
                "REQ-2026-000001", "CAND-000001")).thenReturn(List.of());
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createApplication(
                new ApplicationRequest("REQ-2026-000001", "CAND-000001", null));

        assertTrue(response.id().startsWith("APP-"));
        assertEquals("REQ-2026-000001", response.requisitionId());
        assertEquals("CAND-000001", response.candidateId());
        assertEquals(com.hackathon.api.enums.ApplicationStage.APPLIED, response.stage());
        verify(stageHistoryRepository).save(any(ApplicationStageHistory.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
        verify(dataIssueRepository, never()).save(any(DataIssue.class));
    }

    @Test
    void createApplicationRejectsMissingRequisition() {
        when(requisitionRepository.findById("REQ-MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createApplication(
                        new ApplicationRequest("REQ-MISSING", "CAND-000001", null)));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplicationRejectsUnapprovedRequisition() {
        approvedRequisition = new JobRequisition(
                "REQ-2026-000001",
                approvedRequisition.getDepartment(),
                "Software Engineer", "Build services", EmploymentType.FULL_TIME,
                "Hyderabad", 2, new BigDecimal("600000"),
                new BigDecimal("1200000"), "INR", "requester@example.test");
        when(requisitionRepository.findById("REQ-2026-000001"))
                .thenReturn(Optional.of(approvedRequisition));

        assertThrows(IllegalStateException.class,
                () -> service.createApplication(
                        new ApplicationRequest("REQ-2026-000001", "CAND-000001", null)));

        verify(candidateRepository, never()).findById(any());
        verify(applicationRepository, never()).save(any(Application.class));
    }
}
