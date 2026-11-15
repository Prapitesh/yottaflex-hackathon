package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.feedback.SelectApplicationRequest;
import com.hackathon.api.dto.request.feedback.SubmitFeedbackRequest;
import com.hackathon.api.dto.response.feedback.FeedbackResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.InterviewStatus;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.FeedbackService;
import com.hackathon.api.policy.RecruitmentPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewInterviewerRepository panelRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStageHistoryRepository historyRepository;
    private final AuditEventRepository auditRepository;

    public FeedbackServiceImpl(
            FeedbackRepository feedbackRepository,
            InterviewRepository interviewRepository,
            InterviewInterviewerRepository panelRepository,
            ApplicationRepository applicationRepository,
            ApplicationStageHistoryRepository historyRepository,
            AuditEventRepository auditRepository) {
        this.feedbackRepository = feedbackRepository;
        this.interviewRepository = interviewRepository;
        this.panelRepository = panelRepository;
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public FeedbackResponse submitFeedback(String interviewId, SubmitFeedbackRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));

        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Feedback can only be submitted after the interview is completed");
        }
        if (!panelRepository.existsByInterview_IdAndInterviewer_Id(
                interviewId, request.interviewerId())) {
            throw new IllegalStateException(
                    "Only an interviewer assigned to this interview can submit feedback");
        }

        InterviewInterviewer assignment = panelRepository.findByInterview_Id(interviewId).stream()
                .filter(item -> item.getInterviewer().getId().equals(request.interviewerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Assigned interviewer record could not be loaded"));

        if (feedbackRepository.findByInterview_IdAndInterviewer_Id(
                interviewId, request.interviewerId()).isPresent()) {
            throw new DuplicateResourceException("Feedback already exists for this interviewer and interview");
        }

        Interviewer interviewer = assignment.getInterviewer();
        Feedback feedback = new Feedback(
                EntityIdGenerator.feedbackId(interviewId, interviewer.getId()),
                interview, interviewer, request.score(), request.recommendation(),
                request.strengths().trim(), normalize(request.concerns()));
        Feedback saved = feedbackRepository.save(feedback);

        auditRepository.save(new AuditEvent(
                "FEEDBACK", saved.getId(), "FEEDBACK_SUBMITTED", interviewer.getId(),
                Map.of(
                        "interviewId", interviewId,
                        "applicationId", interview.getApplication().getId(),
                        "score", request.score(),
                        "recommendation", request.recommendation().name()
                )));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackForInterview(String interviewId) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new ResourceNotFoundException("Interview not found: " + interviewId);
        }
        return feedbackRepository.findByInterview_Id(interviewId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public void selectApplication(String applicationId, SelectApplicationRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found: " + applicationId));
        if (application.getStage() != ApplicationStage.INTERVIEW) {
            throw new IllegalStateException("Only applications in INTERVIEW can be selected");
        }

        List<Interview> interviews =
                interviewRepository.findByApplication_IdOrderByScheduledStartDesc(applicationId);
        if (interviews.isEmpty()) {
            throw new IllegalStateException("At least one completed interview is required before selection");
        }

        int requiredFeedback = 0;
        int submittedFeedback = 0;
        for (Interview interview : interviews) {
            if (interview.getStatus() == InterviewStatus.CANCELLED) {
                continue;
            }
            if (interview.getStatus() != InterviewStatus.COMPLETED) {
                throw new IllegalStateException(
                        "All non-cancelled interviews must be completed before selection");
            }
            requiredFeedback += panelRepository.findByInterview_Id(interview.getId()).size();
            submittedFeedback += feedbackRepository.findByInterview_Id(interview.getId()).size();
        }

        if (requiredFeedback == 0
                || !RecruitmentPolicy.hasRequiredFeedback(submittedFeedback, requiredFeedback)) {
            throw new IllegalStateException(
                    "Selection is blocked until every assigned interviewer submits feedback");
        }

        RecruitmentPolicy.requireValidTransition(
                ApplicationStage.INTERVIEW, ApplicationStage.SELECTED);
        application.changeStage(ApplicationStage.SELECTED, OffsetDateTime.now());
        applicationRepository.save(application);
        historyRepository.save(new ApplicationStageHistory(
                application, ApplicationStage.INTERVIEW, ApplicationStage.SELECTED,
                request.actor(), request.reason().trim()));
        auditRepository.save(new AuditEvent(
                "APPLICATION", applicationId, "APPLICATION_SELECTED", request.actor(),
                Map.of(
                        "reason", request.reason().trim(),
                        "completedInterviewCount", interviews.stream()
                                .filter(i -> i.getStatus() == InterviewStatus.COMPLETED).count(),
                        "submittedFeedbackCount", submittedFeedback
                )));
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(), feedback.getInterview().getId(),
                feedback.getInterviewer().getId(), feedback.getInterviewer().getFullName(),
                feedback.getScore(), feedback.getRecommendation(),
                feedback.getStrengths(), feedback.getConcerns(), feedback.getSubmittedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
