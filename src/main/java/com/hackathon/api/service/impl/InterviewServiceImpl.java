package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.interview.*;
import com.hackathon.api.dto.response.interview.*;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.InterviewService;
import com.hackathon.api.policy.RecruitmentPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Coordinates interview scheduling and lifecycle changes.
 * All availability checks and writes run in one transaction.
 */
@Service
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private static final List<InterviewStatus> ACTIVE_STATUSES =
            List.of(InterviewStatus.SCHEDULED, InterviewStatus.RESCHEDULED);

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewerRepository interviewerRepository;
    private final InterviewInterviewerRepository panelRepository;
    private final ApplicationStageHistoryRepository stageHistoryRepository;
    private final AuditEventRepository auditEventRepository;

    public InterviewServiceImpl(
            InterviewRepository interviewRepository,
            ApplicationRepository applicationRepository,
            InterviewerRepository interviewerRepository,
            InterviewInterviewerRepository panelRepository,
            ApplicationStageHistoryRepository stageHistoryRepository,
            AuditEventRepository auditEventRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.interviewerRepository = interviewerRepository;
        this.panelRepository = panelRepository;
        this.stageHistoryRepository = stageHistoryRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public InterviewResponse scheduleInterview(ScheduleInterviewRequest request) {
        validateInterval(request.scheduledStart(), request.scheduledEnd(), true);
        validatePanel(request.panel());

        Application application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found: " + request.applicationId()));

        ApplicationStage currentStage = application.getStage();
        if (currentStage != ApplicationStage.SHORTLISTED
                && currentStage != ApplicationStage.INTERVIEW) {
            throw new IllegalStateException(
                    "Interview can only be scheduled for SHORTLISTED or INTERVIEW applications");
        }

        List<Interviewer> panel = lockAndValidatePanel(request.panel());
        ensureNoConflicts(panel, request.scheduledStart(), request.scheduledEnd(), null);

        Interview interview = new Interview(
                EntityIdGenerator.interviewId(),
                application,
                request.interviewType(),
                request.scheduledStart(),
                request.scheduledEnd(),
                request.mode(),
                normalize(request.meetingLocation())
        );
        Interview saved = interviewRepository.save(interview);

        List<InterviewInterviewer> assignments = new ArrayList<>();
        Map<String, InterviewPanelistRequest> requestedPanel = request.panel().stream()
                .collect(Collectors.toMap(
                        InterviewPanelistRequest::interviewerId,
                        Function.identity()));
        for (Interviewer interviewer : panel) {
            assignments.add(new InterviewInterviewer(
                    saved, interviewer, requestedPanel.get(interviewer.getId()).role()));
        }
        panelRepository.saveAll(assignments);

        if (currentStage == ApplicationStage.SHORTLISTED) {
            RecruitmentPolicy.requireValidTransition(
                    ApplicationStage.SHORTLISTED, ApplicationStage.INTERVIEW);
            application.changeStage(ApplicationStage.INTERVIEW, OffsetDateTime.now());
            applicationRepository.save(application);
            stageHistoryRepository.save(new ApplicationStageHistory(
                    application,
                    ApplicationStage.SHORTLISTED,
                    ApplicationStage.INTERVIEW,
                    request.actor(),
                    "Interview scheduled: " + saved.getId()
            ));
        }

        auditEventRepository.save(new AuditEvent(
                "INTERVIEW", saved.getId(), "INTERVIEW_SCHEDULED", request.actor(),
                Map.of(
                        "applicationId", application.getId(),
                        "interviewType", saved.getInterviewType().name(),
                        "scheduledStart", saved.getScheduledStart().toString(),
                        "scheduledEnd", saved.getScheduledEnd().toString(),
                        "panelInterviewerIds", panel.stream().map(Interviewer::getId).toList()
                )
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(String interviewId) {
        return toResponse(findInterview(interviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsForApplication(String applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }
        return interviewRepository.findByApplication_IdOrderByScheduledStartDesc(applicationId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public InterviewResponse rescheduleInterview(
            String interviewId, RescheduleInterviewRequest request) {
        Interview interview = findInterview(interviewId);
        requireActive(interview);
        validateInterval(request.scheduledStart(), request.scheduledEnd(), true);

        List<InterviewInterviewer> assignments = panelRepository.findByInterview_Id(interviewId);
        List<Interviewer> lockedPanel = lockExistingPanel(assignments);
        ensureNoConflicts(
                lockedPanel, request.scheduledStart(), request.scheduledEnd(), interviewId);

        interview.reschedule(request.scheduledStart(), request.scheduledEnd());
        Interview saved = interviewRepository.save(interview);
        auditEventRepository.save(new AuditEvent(
                "INTERVIEW", saved.getId(), "INTERVIEW_RESCHEDULED", request.actor(),
                Map.of(
                        "reason", request.reason(),
                        "scheduledStart", saved.getScheduledStart().toString(),
                        "scheduledEnd", saved.getScheduledEnd().toString()
                )
        ));
        return toResponse(saved);
    }

    @Override
    public InterviewResponse cancelInterview(
            String interviewId, InterviewActionRequest request) {
        Interview interview = findInterview(interviewId);
        requireActive(interview);
        interview.cancel();
        Interview saved = interviewRepository.save(interview);
        recordAction(saved, "INTERVIEW_CANCELLED", request);
        return toResponse(saved);
    }

    @Override
    public InterviewResponse completeInterview(
            String interviewId, InterviewActionRequest request) {
        Interview interview = findInterview(interviewId);
        requireActive(interview);
        interview.complete();
        Interview saved = interviewRepository.save(interview);
        recordAction(saved, "INTERVIEW_COMPLETED", request);
        return toResponse(saved);
    }

    private List<Interviewer> lockAndValidatePanel(
            List<InterviewPanelistRequest> requestedPanel) {
        List<String> ids = requestedPanel.stream()
                .map(InterviewPanelistRequest::interviewerId)
                .sorted()
                .toList();

        Map<String, Interviewer> locked = new HashMap<>();
        for (String id : ids) {
            Interviewer interviewer = interviewerRepository.findByIdForScheduling(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Interviewer not found: " + id));
            if (!interviewer.isActive()) {
                throw new IllegalStateException(
                        "Interviewer is inactive and cannot be scheduled: " + id);
            }
            locked.put(id, interviewer);
        }
        return requestedPanel.stream()
                .map(item -> locked.get(item.interviewerId()))
                .toList();
    }

    private List<Interviewer> lockExistingPanel(List<InterviewInterviewer> assignments) {
        List<String> ids = assignments.stream()
                .map(assignment -> assignment.getInterviewer().getId())
                .sorted()
                .toList();
        List<Interviewer> locked = new ArrayList<>();
        for (String id : ids) {
            Interviewer interviewer = interviewerRepository.findByIdForScheduling(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Interviewer not found: " + id));
            if (!interviewer.isActive()) {
                throw new IllegalStateException(
                        "An assigned interviewer is inactive: " + id);
            }
            locked.add(interviewer);
        }
        return locked;
    }

    private void ensureNoConflicts(
            List<Interviewer> panel,
            OffsetDateTime start,
            OffsetDateTime end,
            String excludedInterviewId) {
        for (Interviewer interviewer : panel) {
            List<Interview> conflicts =
                    interviewRepository.findConflictingInterviewsForInterviewer(
                            interviewer.getId(), ACTIVE_STATUSES,
                            start, end, excludedInterviewId);
            if (!conflicts.isEmpty()) {
                Interview conflict = conflicts.getFirst();
                throw new IllegalStateException(
                        "Interviewer " + interviewer.getId()
                                + " has an overlapping interview: " + conflict.getId());
            }
        }
    }

    private void validatePanel(List<InterviewPanelistRequest> panel) {
        if (panel == null || panel.isEmpty()) {
            throw new IllegalArgumentException("At least one interviewer is required");
        }
        Set<String> ids = new HashSet<>();
        long leadCount = 0;
        for (InterviewPanelistRequest member : panel) {
            if (member == null || member.interviewerId() == null
                    || member.interviewerId().isBlank() || member.role() == null) {
                throw new IllegalArgumentException("Every panel member needs an ID and role");
            }
            if (!ids.add(member.interviewerId())) {
                throw new IllegalArgumentException(
                        "An interviewer cannot be assigned to the same panel twice");
            }
            if (member.role() == InterviewPanelRole.LEAD) {
                leadCount++;
            }
        }
        if (leadCount != 1) {
            throw new IllegalArgumentException(
                    "An interview panel must have exactly one LEAD interviewer");
        }
    }

    private void validateInterval(
            OffsetDateTime start, OffsetDateTime end, boolean requireFutureStart) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "Interview end time must be after its start time");
        }
        if (requireFutureStart && !start.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Interview must be scheduled in the future");
        }
    }

    private void requireActive(Interview interview) {
        if (!ACTIVE_STATUSES.contains(interview.getStatus())) {
            throw new IllegalStateException(
                    "Only scheduled or rescheduled interviews can be changed");
        }
    }

    private void recordAction(
            Interview interview, String eventType, InterviewActionRequest request) {
        auditEventRepository.save(new AuditEvent(
                "INTERVIEW", interview.getId(), eventType, request.actor(),
                Map.of("reason", request.reason(), "status", interview.getStatus().name())
        ));
    }

    private Interview findInterview(String interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview not found: " + interviewId));
    }

    private InterviewResponse toResponse(Interview interview) {
        List<InterviewPanelistResponse> panel = panelRepository
                .findByInterview_Id(interview.getId())
                .stream()
                .map(assignment -> new InterviewPanelistResponse(
                        assignment.getInterviewer().getId(),
                        assignment.getInterviewer().getFullName(),
                        assignment.getInterviewer().getEmail(),
                        assignment.getRole()
                ))
                .toList();

        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getInterviewType(),
                interview.getScheduledStart(),
                interview.getScheduledEnd(),
                interview.getMode(),
                interview.getMeetingLocation(),
                interview.getStatus(),
                panel
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
