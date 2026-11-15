package com.hackathon.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/** Explicit record of a known or detected data-quality anomaly. */
@Entity
@Table(name = "data_issue")
public class DataIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "issue_type", nullable = false, length = 80)
    private String issueType;

    @NotBlank
    @Size(max = 60)
    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @NotBlank
    @Size(max = 40)
    @Column(name = "ground_truth_label", nullable = false, length = 40)
    private String groundTruthLabel;

    @NotBlank
    @Column(name = "expected_decision", nullable = false, columnDefinition = "TEXT")
    private String expectedDecision;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> details;

    @NotNull
    @Column(name = "detected_at", nullable = false, updatable = false)
    private OffsetDateTime detectedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    protected DataIssue() {
    }

    public DataIssue(String issueType, String entityType, String entityId,
                     String groundTruthLabel, String expectedDecision,
                     Map<String, Object> details) {
        this.issueType = issueType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.groundTruthLabel = groundTruthLabel;
        this.expectedDecision = expectedDecision;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = OffsetDateTime.now();
        }
    }

    public void resolve(OffsetDateTime resolvedAt) {
        if (resolvedAt == null || (detectedAt != null && resolvedAt.isBefore(detectedAt))) {
            throw new IllegalArgumentException("Resolution time must be on or after detection");
        }
        this.resolvedAt = resolvedAt;
    }

    public Long getId() { return id; }
    public String getIssueType() { return issueType; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getGroundTruthLabel() { return groundTruthLabel; }
    public String getExpectedDecision() { return expectedDecision; }
    public Map<String, Object> getDetails() { return details; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
}
