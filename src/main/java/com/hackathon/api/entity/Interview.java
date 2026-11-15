package com.hackathon.api.entity;

import com.hackathon.api.enums.InterviewMode;
import com.hackathon.api.enums.InterviewStatus;
import com.hackathon.api.enums.InterviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** A scheduled assessment event for a single application. */
@Entity
@Table(name = "interview")
public class Interview {

    @Id
    @Size(max = 30)
    @Column(name = "interview_id", nullable = false, length = 30)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 24)
    private InterviewType interviewType;

    @NotNull
    @Column(name = "scheduled_start", nullable = false)
    private OffsetDateTime scheduledStart;

    @NotNull
    @Column(name = "scheduled_end", nullable = false)
    private OffsetDateTime scheduledEnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InterviewMode mode;

    @Column(name = "meeting_location", columnDefinition = "TEXT")
    private String meetingLocation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Interview() {
    }

    public Interview(String id, Application application, InterviewType interviewType,
                     OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd,
                     InterviewMode mode, String meetingLocation) {
        this.id = id;
        this.application = application;
        this.interviewType = interviewType;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.mode = mode;
        this.meetingLocation = meetingLocation;
        this.status = InterviewStatus.SCHEDULED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public void reschedule(OffsetDateTime start, OffsetDateTime end) {
        this.scheduledStart = start;
        this.scheduledEnd = end;
        this.status = InterviewStatus.RESCHEDULED;
    }

    public void complete() { this.status = InterviewStatus.COMPLETED; }
    public void cancel() { this.status = InterviewStatus.CANCELLED; }
    public void markNoShow() { this.status = InterviewStatus.NO_SHOW; }

    @AssertTrue(message = "Interview end must be after its start")
    public boolean isScheduleValid() {
        return scheduledStart == null || scheduledEnd == null
                || scheduledEnd.isAfter(scheduledStart);
    }

    public String getId() { return id; }
    public Application getApplication() { return application; }
    public InterviewType getInterviewType() { return interviewType; }
    public OffsetDateTime getScheduledStart() { return scheduledStart; }
    public OffsetDateTime getScheduledEnd() { return scheduledEnd; }
    public InterviewMode getMode() { return mode; }
    public String getMeetingLocation() { return meetingLocation; }
    public InterviewStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
