package com.hackathon.api.entity;

import com.hackathon.api.enums.FeedbackRecommendation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Structured feedback submitted by one interviewer for one interview. */
@Entity
@Table(
        name = "feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feedback_interview_interviewer",
                columnNames = {"interview_id", "interviewer_id"}
        )
)
public class Feedback {

    @Id
    @NotBlank
    @Size(max = 60)
    @Column(name = "feedback_id", nullable = false, length = 60)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("10.00")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal score;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackRecommendation recommendation;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String concerns;

    @NotNull
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    protected Feedback() {
    }

    public Feedback(String id, Interview interview, Interviewer interviewer,
                    BigDecimal score, FeedbackRecommendation recommendation,
                    String strengths, String concerns) {
        this.id = id;
        this.interview = interview;
        this.interviewer = interviewer;
        this.score = score;
        this.recommendation = recommendation;
        this.strengths = strengths;
        this.concerns = concerns;
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = OffsetDateTime.now();
        }
    }

    public void revise(BigDecimal score, FeedbackRecommendation recommendation,
                       String strengths, String concerns) {
        this.score = score;
        this.recommendation = recommendation;
        this.strengths = strengths;
        this.concerns = concerns;
    }

    public String getId() { return id; }
    public Interview getInterview() { return interview; }
    public Interviewer getInterviewer() { return interviewer; }
    public BigDecimal getScore() { return score; }
    public FeedbackRecommendation getRecommendation() { return recommendation; }
    public String getStrengths() { return strengths; }
    public String getConcerns() { return concerns; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
}
