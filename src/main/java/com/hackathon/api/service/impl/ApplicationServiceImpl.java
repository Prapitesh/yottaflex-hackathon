package com.hackathon.api.service.impl;

import com.hackathon.api.service.ApplicationService;

import com.hackathon.api.dto.request.application.ApplicationDecisionRequest;
import com.hackathon.api.dto.request.application.ApplicationRequest;
import com.hackathon.api.dto.response.application.ApplicationResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.OfferStatus;
import com.hackathon.api.policy.RecruitmentPolicy;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Coordinates applications while preserving lifecycle history and audit evidence.
 */
@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRequisitionRepository requisitionRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeArtifactRepository resumeArtifactRepository;
    private final ApplicationStageHistoryRepository stageHistoryRepository;
    private final AuditEventRepository auditEventRepository;
    private final DataIssueRepository dataIssueRepository;
    private final OfferRepository offerRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            JobRequisitionRepository requisitionRepository,
            CandidateRepository candidateRepository,
            ResumeArtifactRepository resumeArtifactRepository,
            ApplicationStageHistoryRepository stageHistoryRepository,
            AuditEventRepository auditEventRepository,
            DataIssueRepository dataIssueRepository,
            OfferRepository offerRepository) {
        this.applicationRepository = applicationRepository;
        this.requisitionRepository = requisitionRepository;
        this.candidateRepository = candidateRepository;
        this.resumeArtifactRepository = resumeArtifactRepository;
        this.stageHistoryRepository = stageHistoryRepository;
        this.auditEventRepository = auditEventRepository;
        this.dataIssueRepository = dataIssueRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public ApplicationResponse createApplication(ApplicationRequest request) {
        JobRequisition requisition = requisitionRepository.findById(request.requisitionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requisition not found: " + request.requisitionId()));

        if (!RecruitmentPolicy.canAcceptApplication(requisition.getStatus())) {
            throw new IllegalStateException(
                    "Applications can only be submitted to approved requisitions");
        }

        Candidate candidate = candidateRepository.findById(request.candidateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found: " + request.candidateId()));

        ResumeArtifact resume = null;
        if (request.resumeId() != null && !request.resumeId().isBlank()) {
            resume = resumeArtifactRepository.findById(request.resumeId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resume not found: " + request.resumeId()));

            if (!resume.getCandidate().getId().equals(candidate.getId())) {
                throw new IllegalArgumentException(
                        "The selected resume does not belong to this candidate");
            }
        }

        List<Application> previousApplications =
                applicationRepository.findByRequisition_IdAndCandidate_Id(
                        requisition.getId(), candidate.getId());

        Application application = new Application(
                EntityIdGenerator.applicationId(requisition.getId(), candidate.getId()),
                requisition,
                candidate,
                resume
        );

        Application saved = applicationRepository.save(application);

        // Keep duplicates visible for the data-quality workflow; do not silently discard them.
        if (!previousApplications.isEmpty()) {
            dataIssueRepository.save(new DataIssue(
                    "DUPLICATE_APPLICATION",
                    "APPLICATION",
                    saved.getId(),
                    "DUPLICATE",
                    "Flag this application for review while preserving the submitted record.",
                    Map.of(
                            "requisitionId", requisition.getId(),
                            "candidateId", candidate.getId(),
                            "previousApplicationCount", previousApplications.size()
                    )
            ));
        }

        stageHistoryRepository.save(new ApplicationStageHistory(
                saved, null, ApplicationStage.APPLIED,
                "SYSTEM", "Application submitted"
        ));

        auditEventRepository.save(new AuditEvent(
                "APPLICATION",
                saved.getId(),
                "APPLICATION_CREATED",
                "SYSTEM",
                Map.of(
                        "requisitionId", requisition.getId(),
                        "candidateId", candidate.getId(),
                        "duplicateDetected", !previousApplications.isEmpty()
                )
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(
            String requisitionId, String candidateId) {
        List<Application> applications;

        boolean hasRequisition = requisitionId != null && !requisitionId.isBlank();
        boolean hasCandidate = candidateId != null && !candidateId.isBlank();

        if (hasRequisition && hasCandidate) {
            applications = applicationRepository
                    .findByRequisition_IdAndCandidate_Id(requisitionId, candidateId);
        } else if (hasRequisition) {
            applications = applicationRepository.findByRequisition_Id(requisitionId);
        } else if (hasCandidate) {
            applications = applicationRepository.findByCandidate_Id(candidateId);
        } else {
            applications = applicationRepository.findAll();
        }

        return applications.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(String applicationId) {
        return toResponse(findApplication(applicationId));
    }
    @Override
    public ApplicationResponse decideApplication(
            String applicationId, ApplicationDecisionRequest request) {
        Application application = findApplication(applicationId);
        ApplicationStage target = request.targetStage();
        if (target != ApplicationStage.SHORTLISTED && target != ApplicationStage.REJECTED) {
            throw new IllegalArgumentException(
                    "Recruiter decision must move the application to SHORTLISTED or REJECTED");
        }

        ApplicationStage previous = application.getStage();
        if (previous != ApplicationStage.SCREENING
                && previous != ApplicationStage.SHORTLISTED
                && previous != ApplicationStage.INTERVIEW) {
            throw new IllegalStateException(
                    "Recruiter decisions are only available after screening or during interview review");
        }
        RecruitmentPolicy.requireValidTransition(previous, target);

        OffsetDateTime changedAt = OffsetDateTime.now();
        application.changeStage(target, changedAt);
        Application saved = applicationRepository.save(application);
        stageHistoryRepository.save(new ApplicationStageHistory(
                saved, previous, target, request.actor().trim(), request.reason().trim()));
        auditEventRepository.save(new AuditEvent(
                "APPLICATION",
                saved.getId(),
                target == ApplicationStage.SHORTLISTED
                        ? "APPLICATION_SHORTLISTED" : "APPLICATION_REJECTED",
                request.actor().trim(),
                Map.of("fromStage", previous.name(), "toStage", target.name(),
                        "reason", request.reason().trim())
        ));
        return toResponse(saved);
    }
    @Override
    public ApplicationResponse withdrawApplication(
            String applicationId, String actor, String reason) {
        Application application = findApplication(applicationId);
        RecruitmentPolicy.requireValidTransition(
                application.getStage(), ApplicationStage.WITHDRAWN);

        OffsetDateTime changedAt = OffsetDateTime.now();
        ApplicationStage previousStage = application.getStage();
        if (previousStage == ApplicationStage.SELECTED || previousStage == ApplicationStage.OFFERED) {
            offerRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId).stream()
                    .filter(offer -> List.of(OfferStatus.DRAFT, OfferStatus.PENDING_APPROVAL,
                            OfferStatus.APPROVED, OfferStatus.ISSUED).contains(offer.getStatus()))
                    .forEach(offer -> {
                        offer.withdraw();
                        offerRepository.save(offer);
                        auditEventRepository.save(new AuditEvent("OFFER", offer.getId(),
                                "OFFER_WITHDRAWN", actor.trim(),
                                Map.of("applicationId", applicationId, "reason", reason.trim())));
                    });
        }
        application.changeStage(ApplicationStage.WITHDRAWN, changedAt);
        Application saved = applicationRepository.save(application);

        stageHistoryRepository.save(new ApplicationStageHistory(
                saved, previousStage, ApplicationStage.WITHDRAWN,
                actor.trim(), reason.trim()
        ));

        auditEventRepository.save(new AuditEvent(
                "APPLICATION",
                saved.getId(),
                "APPLICATION_WITHDRAWN",
                actor.trim(),
                Map.of("fromStage", previousStage.name(), "reason", reason.trim())
        ));

        return toResponse(saved);
    }

    private Application findApplication(String id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found: " + id));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getRequisition().getId(),
                application.getCandidate().getId(),
                application.getResume() == null ? null : application.getResume().getId(),
                application.getStage(),
                application.getAppliedAt(),
                application.getLastStageChangedAt()
        );
    }
}
