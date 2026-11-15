package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.interview.*;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InterviewServiceImplTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private InterviewerRepository interviewerRepository;
    @Mock private InterviewInterviewerRepository panelRepository;
    @Mock private ApplicationStageHistoryRepository stageHistoryRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private InterviewServiceImpl service;
    private Application application;
    private Interviewer interviewer;
    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new InterviewServiceImpl(
                interviewRepository, applicationRepository, interviewerRepository,
                panelRepository, stageHistoryRepository, auditEventRepository);

        Department department = new Department("DEP-000001", "Engineering", "ENG-01");
        JobRequisition requisition = new JobRequisition(
                "REQ-2026-000001", department, "Java Developer", "Build APIs",
                EmploymentType.FULL_TIME, "Hyderabad", 1,
                new BigDecimal("600000"), new BigDecimal("1200000"),
                "INR", "requester@example.test");
        requisition.approve("approver@example.test", OffsetDateTime.now());

        Candidate candidate = new Candidate(
                "CAND-000001", "Asha Rao", "asha@example.test",
                null, "Hyderabad", 24);
        application = new Application(
                "APP-REQ-2026-000001-CAND-000001",
                requisition, candidate, null);
        application.changeStage(ApplicationStage.SHORTLISTED, OffsetDateTime.now());

        interviewer = new Interviewer(
                "INTV-000001", "Ravi Kumar", "ravi@example.test",
                department, "Asia/Kolkata");

        start = OffsetDateTime.now().plusDays(2);
        end = start.plusHours(1);

        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(interviewerRepository.findByIdForScheduling(interviewer.getId()))
                .thenReturn(Optional.of(interviewer));
        when(interviewRepository.findConflictingInterviewsForInterviewer(
                anyString(), anyList(), any(), any(), any()))
                .thenReturn(List.of());
        when(interviewRepository.save(any(Interview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(panelRepository.findByInterview_Id(anyString()))
                .thenAnswer(invocation -> {
                    Interview interview = interviewRepository
                            .findById(invocation.getArgument(0)).orElse(null);
                    return interview == null ? List.of()
                            : List.of(new InterviewInterviewer(
                                    interview, interviewer, InterviewPanelRole.LEAD));
                });
        when(interviewRepository.findById(anyString())).thenAnswer(invocation ->
                Optional.empty());
    }

    @Test
    void scheduleCreatesInterviewPanelHistoryAndAudit() {
        when(interviewRepository.findById(anyString())).thenAnswer(invocation -> {
            // The generated interview is returned by save and panel lookup is mocked below.
            return Optional.empty();
        });
        when(panelRepository.findByInterview_Id(anyString())).thenReturn(List.of());

        var response = service.scheduleInterview(validRequest());

        assertTrue(response.id().startsWith("INT-"));
        assertEquals(application.getId(), response.applicationId());
        assertEquals(InterviewStatus.SCHEDULED, response.status());
        assertEquals(ApplicationStage.INTERVIEW, application.getStage());
        verify(interviewRepository).save(any(Interview.class));
        verify(panelRepository).saveAll(anyList());
        verify(applicationRepository).save(application);
        verify(stageHistoryRepository).save(any(ApplicationStageHistory.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void schedulingRejectsOverlappingInterviewerSlot() {
        when(interviewRepository.findConflictingInterviewsForInterviewer(
                eq(interviewer.getId()), anyList(), any(), any(), isNull()))
                .thenReturn(List.of(new Interview(
                        "INT-2026-EXISTING", application, InterviewType.TECHNICAL,
                        start.minusMinutes(30), end.minusMinutes(30),
                        InterviewMode.ONLINE, null)));

        assertThrows(IllegalStateException.class,
                () -> service.scheduleInterview(validRequest()));

        verify(interviewRepository, never()).save(any(Interview.class));
        verify(panelRepository, never()).saveAll(anyList());
    }

    @Test
    void schedulingRejectsInactiveInterviewer() {
        interviewer.deactivate();

        assertThrows(IllegalStateException.class,
                () -> service.scheduleInterview(validRequest()));

        verify(interviewRepository, never()).save(any(Interview.class));
    }

    @Test
    void schedulingRequiresExactlyOneLead() {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
                application.getId(), InterviewType.TECHNICAL,
                start, end, InterviewMode.ONLINE, null, "recruiter@example.test",
                List.of(new InterviewPanelistRequest(
                        interviewer.getId(), InterviewPanelRole.PANELIST)));

        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleInterview(request));

        verify(applicationRepository, never()).findById(anyString());
    }

    @Test
    void schedulingRejectsApplicationThatIsNotShortlistedOrInInterview() {
        application.changeStage(ApplicationStage.SCREENING, OffsetDateTime.now());

        assertThrows(IllegalStateException.class,
                () -> service.scheduleInterview(validRequest()));

        verify(interviewRepository, never()).save(any(Interview.class));
    }

    @Test
    void schedulingRejectsMissingApplication() {
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.scheduleInterview(validRequest()));

        verify(interviewRepository, never()).save(any(Interview.class));
    }

    private ScheduleInterviewRequest validRequest() {
        return new ScheduleInterviewRequest(
                application.getId(),
                InterviewType.TECHNICAL,
                start,
                end,
                InterviewMode.ONLINE,
                "Video conference",
                "recruiter@example.test",
                List.of(new InterviewPanelistRequest(
                        interviewer.getId(), InterviewPanelRole.LEAD))
        );
    }
}
