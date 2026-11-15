package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.feedback.*;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.exception.DuplicateResourceException;
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

class FeedbackServiceImplTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private InterviewInterviewerRepository panelRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationStageHistoryRepository stageHistoryRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private FeedbackServiceImpl service;
    private Interview interview;
    private Application application;
    private Interviewer interviewer;
    private InterviewInterviewer assignment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FeedbackServiceImpl(
                feedbackRepository, interviewRepository, panelRepository,
                applicationRepository, stageHistoryRepository, auditEventRepository);

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
        application.changeStage(ApplicationStage.INTERVIEW, OffsetDateTime.now());

        interviewer = new Interviewer(
                "INTV-000001", "Ravi Kumar", "ravi@example.test",
                department, "Asia/Kolkata");
        interview = new Interview(
                "INT-2026-000001", application, InterviewType.TECHNICAL,
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(1),
                InterviewMode.ONLINE, null);
        interview.complete();
        assignment = new InterviewInterviewer(
                interview, interviewer, InterviewPanelRole.LEAD);

        when(interviewRepository.findById(interview.getId()))
                .thenReturn(Optional.of(interview));
        when(panelRepository.existsByInterview_IdAndInterviewer_Id(
                interview.getId(), interviewer.getId())).thenReturn(true);
        when(panelRepository.findByInterview_Id(interview.getId()))
                .thenReturn(List.of(assignment));
        when(feedbackRepository.findByInterview_IdAndInterviewer_Id(
                interview.getId(), interviewer.getId())).thenReturn(Optional.empty());
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void assignedInterviewerCanSubmitStructuredFeedback() {
        var response = service.submitFeedback(interview.getId(), validRequest());

        assertEquals("FB-" + interview.getId() + "-" + interviewer.getId(), response.id());
        assertEquals(new BigDecimal("8.50"), response.score());
        assertEquals(FeedbackRecommendation.STRONG_YES, response.recommendation());
        assertEquals("Strong Java fundamentals", response.strengths());
        verify(feedbackRepository).save(any(Feedback.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void duplicateFeedbackIsRejected() {
        when(feedbackRepository.findByInterview_IdAndInterviewer_Id(
                interview.getId(), interviewer.getId()))
                .thenReturn(Optional.of(mock(Feedback.class)));

        assertThrows(DuplicateResourceException.class,
                () -> service.submitFeedback(interview.getId(), validRequest()));

        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void unassignedInterviewerCannotSubmitFeedback() {
        when(panelRepository.existsByInterview_IdAndInterviewer_Id(
                interview.getId(), interviewer.getId())).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> service.submitFeedback(interview.getId(), validRequest()));

        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void feedbackCannotBeSubmittedBeforeInterviewIsCompleted() {
        interview.reschedule(
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1));

        assertThrows(IllegalStateException.class,
                () -> service.submitFeedback(interview.getId(), validRequest()));

        verify(feedbackRepository, never()).save(any(Feedback.class));
    }

    @Test
    void selectionIsBlockedWhenPanelFeedbackIsMissing() {
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(interviewRepository.findByApplication_IdOrderByScheduledStartDesc(
                application.getId())).thenReturn(List.of(interview));
        when(panelRepository.findByInterview_Id(interview.getId()))
                .thenReturn(List.of(assignment));
        when(feedbackRepository.findByInterview_Id(interview.getId()))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> service.selectApplication(
                        application.getId(),
                        new SelectApplicationRequest("recruiter@example.test", "Good fit")));

        assertEquals(ApplicationStage.INTERVIEW, application.getStage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void selectionSucceedsWhenEveryAssignedInterviewerSubmittedFeedback() {
        Feedback feedback = new Feedback(
                "FB-" + interview.getId() + "-" + interviewer.getId(),
                interview, interviewer, new BigDecimal("8.50"),
                FeedbackRecommendation.STRONG_YES,
                "Strong Java fundamentals", null);

        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(interviewRepository.findByApplication_IdOrderByScheduledStartDesc(
                application.getId())).thenReturn(List.of(interview));
        when(panelRepository.findByInterview_Id(interview.getId()))
                .thenReturn(List.of(assignment));
        when(feedbackRepository.findByInterview_Id(interview.getId()))
                .thenReturn(List.of(feedback));

        service.selectApplication(
                application.getId(),
                new SelectApplicationRequest("recruiter@example.test", "Strong technical feedback"));

        assertEquals(ApplicationStage.SELECTED, application.getStage());
        verify(applicationRepository).save(application);
        verify(stageHistoryRepository).save(any(ApplicationStageHistory.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void selectionIsBlockedWhileAnInterviewIsStillScheduled() {
        Interview pending = new Interview(
                "INT-2026-000002", application, InterviewType.HR,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                InterviewMode.ONLINE, null);

        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(interviewRepository.findByApplication_IdOrderByScheduledStartDesc(
                application.getId())).thenReturn(List.of(interview, pending));

        assertThrows(IllegalStateException.class,
                () -> service.selectApplication(
                        application.getId(),
                        new SelectApplicationRequest("recruiter@example.test", "Proceed")));

        verify(applicationRepository, never()).save(any(Application.class));
    }

    private SubmitFeedbackRequest validRequest() {
        return new SubmitFeedbackRequest(
                interviewer.getId(),
                new BigDecimal("8.50"),
                FeedbackRecommendation.STRONG_YES,
                "  Strong Java fundamentals  ",
                "Needs more distributed systems exposure"
        );
    }
}
