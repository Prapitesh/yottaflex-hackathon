package com.hackathon.api.entity;

import com.hackathon.api.enums.ApplicationStage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** Append-only record of an application lifecycle transition. */
@Entity
@Table(name = "application_stage_history")
public class ApplicationStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_stage", length = 24)
    private ApplicationStage fromStage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_stage", nullable = false, length = 24)
    private ApplicationStage toStage;

    @NotBlank
    @Size(max = 120)
    @Column(name = "changed_by", nullable = false, length = 120)
    private String changedBy;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    protected ApplicationStageHistory() {
    }

    public ApplicationStageHistory(Application application, ApplicationStage fromStage,
                                    ApplicationStage toStage, String changedBy, String reason) {
        this.application = application;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.changedBy = changedBy;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public ApplicationStage getFromStage() { return fromStage; }
    public ApplicationStage getToStage() { return toStage; }
    public String getChangedBy() { return changedBy; }
    public String getReason() { return reason; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
