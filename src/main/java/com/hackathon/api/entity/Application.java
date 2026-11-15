package com.hackathon.api.entity;

import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.ScreeningDecision;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** A candidate's application to one requisition. Lifecycle is controlled by the service layer. */
@Entity
@Table(name = "application")
public class Application {

    @Id
    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^APP-.+")
    @Column(name = "application_id", nullable = false, length = 100)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisition_id", nullable = false)
    private JobRequisition requisition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private ResumeArtifact resume;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ApplicationStage stage = ApplicationStage.APPLIED;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "screening_score", precision = 5, scale = 2)
    private BigDecimal screeningScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_decision", length = 20)
    private ScreeningDecision screeningDecision;

    @NotNull
    @Column(name = "applied_at", nullable = false, updatable = false)
    private OffsetDateTime appliedAt;

    @NotNull
    @Column(name = "last_stage_changed_at", nullable = false)
    private OffsetDateTime lastStageChangedAt;

    protected Application() {
    }

    public Application(String id, JobRequisition requisition,
                       Candidate candidate, ResumeArtifact resume) {
        this.id = id;
        this.requisition = requisition;
        this.candidate = candidate;
        this.resume = resume;
        this.stage = ApplicationStage.APPLIED;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (appliedAt == null) {
            appliedAt = now;
        }
        if (lastStageChangedAt == null) {
            lastStageChangedAt = appliedAt;
        }
    }

    /** Called by the application service after validating the transition and recording history. */
    public void changeStage(ApplicationStage nextStage, OffsetDateTime changedAt) {
        if (nextStage == null || changedAt == null) {
            throw new IllegalArgumentException("Next stage and change timestamp are required");
        }
        this.stage = nextStage;
        this.lastStageChangedAt = changedAt;
    }

    public void recordScreening(BigDecimal score, ScreeningDecision decision) {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0
                || score.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Screening score must be between 0 and 100");
        }
        if (decision == null) {
            throw new IllegalArgumentException("Screening decision is required");
        }
        this.screeningScore = score;
        this.screeningDecision = decision;
    }

    public String getId() { return id; }
    public JobRequisition getRequisition() { return requisition; }
    public Candidate getCandidate() { return candidate; }
    public ResumeArtifact getResume() { return resume; }
    public ApplicationStage getStage() { return stage; }
    public BigDecimal getScreeningScore() { return screeningScore; }
    public ScreeningDecision getScreeningDecision() { return screeningDecision; }
    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public OffsetDateTime getLastStageChangedAt() { return lastStageChangedAt; }
}
